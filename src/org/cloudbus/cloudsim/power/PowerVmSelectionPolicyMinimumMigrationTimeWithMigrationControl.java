/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * The Minimum Migration Time (MMT) VM selection policy.
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
public class PowerVmSelectionPolicyMinimumMigrationTimeWithMigrationControl extends PowerVmSelectionPolicy {

	/*
	 * (non-Javadoc)
	 * @see
	 * org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#getVmsToMigrate(org.cloudbus
	 * .cloudsim.power.PowerHost)
	 */
	@Override
	public Vm getVmToMigrate(PowerHost host) {
		List<PowerVm> migratableVms = getMigratableVms(host);
		if (migratableVms.isEmpty()) {
			return null;
		}
		Vm vmToMigrate = null; 
		Vm vmToMigrate1 = null; 
                int xx=0;
                
		double minMetric = Double.MAX_VALUE;
		for (Vm vm : migratableVms) {
			if (vm.isInMigration()) {
				continue;
			}
                        //vm.getId();
                        double metric5=0;
                        List<Double> metric = new ArrayList<Double>();
                        if (CloudSim.clock()>= 1200){
                        //System.out.println("i am in");
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vm.getMips())*100);/// Monil
                        

                        //metric5=(float)((metric0+metric1+metric2+metric3+metric4+metric6+metric7+metric8+metric9+metric10)/10);
                        
                        metric5=MathUtil.mean(metric);
                        }
                        //metric5=(double)((metric0+metric1+metric2+metric3+metric4)/5);
                        PowerVm _vm=(PowerVm) vm;
                        //metric5=_vm.getUtilizationMean();
                        double metric6 = Math.sqrt(_vm.getUtilizationVariance());
                        /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil */
                                          
                        
                        /*float metric0 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100;/// Monil
                        float metric1 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100;/// Monil
                        float metric2 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vm.getMips())*100;/// Monil
                        float metric3 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vm.getMips())*100;/// Monil
                        float metric4 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vm.getMips())*100;/// Monil
                        float metric5=(float)((metric0+metric1+metric2+metric3+metric4)/5);
                        /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil */
                        metric6=MathUtil.stDev(metric);
                        //System.out.println(metric6);
			double metric1 = vm.getRam();
			if (metric1 < minMetric) {
                                minMetric = metric1;
                                if (metric5<80){
				//if (metric0<40&&metric1<40&&metric2<40&&metric3<40&&metric4<40){
                                vmToMigrate = vm;
                                xx=1;
                                }else{
				vmToMigrate1 = vm;
                                }
			}
		}

		if (xx==1){
                return vmToMigrate;  
                
                }else {

                return vmToMigrate1;                
                }

	}

}
