/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * The Maximum Correlation (MC) VM selection policy.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerVmSelectionPolicyMaximumCorrelation_sla extends PowerVmSelectionPolicy {

	/** The fallback policy. */
	private PowerVmSelectionPolicy fallbackPolicy;

	/**
	 * Instantiates a new power vm selection policy maximum correlation.
	 * 
	 * @param fallbackPolicy the fallback policy
	 */
	public PowerVmSelectionPolicyMaximumCorrelation_sla(final PowerVmSelectionPolicy fallbackPolicy) {
		super();
		setFallbackPolicy(fallbackPolicy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#
	 * getVmsToMigrate(org.cloudbus .cloudsim.power.PowerHost)
	 */
	@Override
	public Vm getVmToMigrate(final PowerHost host) {
		List<PowerVm> migratableVms = getMigratableVms(host);
		if (migratableVms.isEmpty()) {
			return null;
		}
		List<Double> metrics = null;
		try {
			metrics = getCorrelationCoefficients(getUtilizationMatrix(migratableVms));
		} catch (IllegalArgumentException e) { // the degrees of freedom must be greater than zero
			return getFallbackPolicy().getVmToMigrate(host);
		}
		double maxMetric = Double.MIN_VALUE;
		int maxIndex = 0;
                int maxIndex_new = 0;
                double maxSla=Double.MAX_VALUE;
                double maxSla_new=Double.MAX_VALUE;
                int count=0;
                //System.out.println(" metric:  "+metrics.size()+" size:  "+migratableVms.size());
		for (int i = 0; i < metrics.size(); i++) {
                        Vm vm=migratableVms.get(i);
                        double sla=getSlaViolation(vm);
			double metric = metrics.get(i);
                        //System.out.println(" Host:  "+host.getId()+" VM:  "+vm.getId()+" metric: "+metric+"  sla:"+sla+" maxmetric: "+maxMetric+"  maxsla:"+maxSla);

                        //System.out.println(" VM:  "+i+" value: "+metric+"  ");
			//if (metric > maxMetric) {
                            if (sla<maxSla){
                                //System.out.println(" Host:  "+host.getId()+" VM:  "+vm.getId()+" metric: "+metric+"  sla:"+sla+" maxmetric: "+maxMetric+"  maxsla:"+maxSla);
				if (vm.priority>0) 
                                {
                                    count+=1;
                                    maxSla_new=sla;
                                    maxIndex_new=i;
                                }
                                else{
                                maxMetric = metric;
                                maxSla=sla;
				maxIndex = i;
                                }
                                
                            }
			}
		//}
                //System.out.println("maximum:  "+metrics.get(maxIndex)+"  ");
                //System.exit(0);
                if ( count==metrics.size())return migratableVms.get(maxIndex_new);
                else return migratableVms.get(maxIndex);
	}
        
        /**
	 * Gets the SLA violation for on VM
	 * 
	 * @param vmList the host
	 * @return the utilization matrix
	 */
        protected double getSlaViolation(Vm vm) {

			double vmTotalAllocated = 0;
			double vmTotalRequested = 0;
                        double vmslaViolationtotaltime=0;
			double vmUnderAllocatedDueToMigration = 0;
                        double vmUnderAllocatedDueToMigrationTotal = 0;
                        double number=0;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsInMigration = false;
			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					vmTotalAllocated += previousAllocated * timeDiff;
					vmTotalRequested += previousRequested * timeDiff;

					if (previousAllocated < previousRequested) {
						if (previousIsInMigration) {
							vmUnderAllocatedDueToMigration += (previousRequested - previousAllocated)
									* timeDiff;
                                                        number+=1;
						}
					}
				}
                                //System.out.println("VM ID: "+vm.getId() + " host ID: "+vm.getHost().getId()+" time: "+entry.getTime()+" previous Allocated: "+previousAllocated+" previousRequested: "+previousRequested+" previousTime: "+previousTime+" previousIsInMigration: "+previousIsInMigration);
                                previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime(); 
				previousIsInMigration = entry.isInMigration();
			}
                        

		return number;
	}
        
	/**
	 * Gets the utilization matrix.
	 * 
	 * @param vmList the host
	 * @return the utilization matrix
	 */
	protected double[][] getUtilizationMatrix(final List<PowerVm> vmList) {
		int n = vmList.size();
		int m = getMinUtilizationHistorySize(vmList);
		double[][] utilization = new double[n][m];
		for (int i = 0; i < n; i++) {
			List<Double> vmUtilization = vmList.get(i).getUtilizationHistory();
			for (int j = 0; j < vmUtilization.size(); j++) {
				utilization[i][j] = vmUtilization.get(j);
			}
		}
		return utilization;
	}

	/**
	 * Gets the min utilization history size.
	 * 
	 * @param vmList the vm list
	 * @return the min utilization history size
	 */
	protected int getMinUtilizationHistorySize(final List<PowerVm> vmList) {
		int minSize = Integer.MAX_VALUE;
		for (PowerVm vm : vmList) {
			int size = vm.getUtilizationHistory().size();
			if (size < minSize) {
				minSize = size;
			}
		}
		return minSize;
	}

	/**
	 * Gets the correlation coefficients.
	 * 
	 * @param data the data
	 * @return the correlation coefficients
	 */
	protected List<Double> getCorrelationCoefficients(final double[][] data) {
		int n = data.length;
		int m = data[0].length;
		List<Double> correlationCoefficients = new LinkedList<Double>();
		for (int i = 0; i < n; i++) {
			double[][] x = new double[n - 1][m];
			int k = 0;
			for (int j = 0; j < n; j++) {
				if (j != i) {
					x[k++] = data[j];
				}
			}

			// Transpose the matrix so that it fits the linear model
			double[][] xT = new Array2DRowRealMatrix(x).transpose().getData();

			// RSquare is the "coefficient of determination"
			correlationCoefficients.add(MathUtil.createLinearRegression(xT,	data[i]).calculateRSquared());
		}
		return correlationCoefficients;
	}

	/**
	 * Gets the fallback policy.
	 * 
	 * @return the fallback policy
	 */
	public PowerVmSelectionPolicy getFallbackPolicy() {
		return fallbackPolicy;
	}

	/**
	 * Sets the fallback policy.
	 * 
	 * @param fallbackPolicy the new fallback policy
	 */
	public void setFallbackPolicy(final PowerVmSelectionPolicy fallbackPolicy) {
		this.fallbackPolicy = fallbackPolicy;
	}
        


}
