/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.cloudbus.cloudsim.Vm;
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
public class PowerVmSelectionPolicyMaximumCorrelationWithMigrationControlsd extends PowerVmSelectionPolicy {
              
	/** The fallback policy. */
	private PowerVmSelectionPolicy fallbackPolicy;

	/**
	 * Instantiates a new power vm selection policy maximum correlation.
	 * 
	 * @param fallbackPolicy the fallback policy
	 */
	public PowerVmSelectionPolicyMaximumCorrelationWithMigrationControlsd(final PowerVmSelectionPolicy fallbackPolicy) {
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
                    //System.out.println("i am in");
			return getFallbackPolicy().getVmToMigrate(host);
		}
		double maxMetric = Double.MIN_VALUE;
		int maxIndex = 0;
                int maxIndex1 = 0;
                Vm vm=null;
                int xx=0;
		for (int i = 0; i < metrics.size(); i++) {
                        vm=migratableVms.get(i);
                        
                        /*float metric5=0;
                        if (CloudSim.clock()>= 2700){
                        //System.out.println("i am in");
                        float metric0 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100;/// Monil
                        float metric1 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100;/// Monil
                        float metric2 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vm.getMips())*100;/// Monil
                        float metric3 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vm.getMips())*100;/// Monil
                        float metric4 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vm.getMips())*100;/// Monil
                        float metric6 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1500) / vm.getMips())*100;/// Monil
                        float metric7 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1800) / vm.getMips())*100;/// Monil
                        float metric8 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2100) / vm.getMips())*100;/// Monil
                        float metric9 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2400) / vm.getMips())*100;/// Monil
                        float metric10 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2700) / vm.getMips())*100;/// Monil

                        metric5=(float)((metric0+metric1+metric2+metric3+metric4+metric6+metric7+metric8+metric9+metric10)/10);
                        System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil 
                        } */
                        double metric5=50;
                        if (CloudSim.clock()>= 2700){
                        List<Double> metric = new ArrayList<Double>();
                        //System.out.println("i am in");
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vm.getMips())*100);/// Monil
                        //metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vm.getMips())*100);/// Monil
                        //metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1500) / vm.getMips())*100);/// Monil
                        //metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1800) / vm.getMips())*100);/// Monil
                        //metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2100) / vm.getMips())*100);/// Monil
                        //metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2400) / vm.getMips())*100);/// Monil
                        //metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2700) / vm.getMips())*100);/// Monil

                        //metric5=(float)((metric0+metric1+metric2+metric3+metric4+metric6+metric7+metric8+metric9+metric10)/10);
                        metric5=MathUtil.stDev(metric);
                        /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+" average: "+ metric5);/// Monil */
                        
                        //System.out.println(metric5);
                        }      
                        float metric0 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100;/// Monil
                        float metric1 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100;/// Monil
                        float metric2 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vm.getMips())*100;/// Monil
                        float metric3 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vm.getMips())*100;/// Monil
                        float metric4 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vm.getMips())*100;/// Monil
                        float metric6=(float)((metric0+metric1+metric2+metric3+metric4)/5);
                        /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil */
                        
			double metric = metrics.get(i);
			if (metric > maxMetric) {
				maxMetric = metric;
				
                                if (metric5>4 && metric6<70){
				//if (metric0<40&&metric1<40&&metric2<40&&metric3<40&&metric4<40){
                                maxIndex = i;
                                xx=1;
                                }else{
				maxIndex1 = i;
                                }
                        }
		}
                if (xx==1){
                return migratableVms.get(maxIndex);
                }else {
                return migratableVms.get(maxIndex1);
                }
		
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
