/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.*;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.MathUtil;

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
public class PowerVmSelectionPolicyMinimumMigrationTimeWithMigrationControlsd extends PowerVmSelectionPolicy {

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
                        //List<double> metric = new ArrayList<double>();
                        double metric5=50;
                        if (CloudSim.clock()>= 2700){
                        List<Double> metric = new ArrayList<Double>();
                        //System.out.println("i am in");
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1500) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1800) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2100) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2400) / vm.getMips())*100);/// Monil
                        metric.add((double)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-2700) / vm.getMips())*100);/// Monil

                        //metric5=(float)((metric0+metric1+metric2+metric3+metric4+metric6+metric7+metric8+metric9+metric10)/10);
                        metric5=MathUtil.stDev(metric);
                        /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+" average: "+ metric5);/// Monil */
                        
                        //System.out.println(metric5);
                        }                    
                        
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
                        
                        
			double metric1 = vm.getRam();
			if (metric1 < minMetric) {
                                minMetric = metric1;
                                if (metric5>3){
				//if (metric0<40&&metric1<40&&metric2<40&&metric3<40&&metric4<40){
                                vmToMigrate = vm;
                                xx=1;
                                }else{
				vmToMigrate1 = vm;
                                }
			}
		}

		if (xx==1){
                /*
                float metric0 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vmToMigrate.getMips())*100;/// Monil
                float metric1 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vmToMigrate.getMips())*100;/// Monil
                float metric2 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vmToMigrate.getMips())*100;/// Monil
                float metric3 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vmToMigrate.getMips())*100;/// Monil
                float metric4 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vmToMigrate.getMips())*100;/// Monil
                float metric5=(float)((metric0+metric1+metric2+metric3+metric4)/5);
               /* System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vmToMigrate.getId()+" Ram : "+vmToMigrate.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil */
                return vmToMigrate;  
                
                }else {
                /*
                float metric0 = (float)(vmToMigrate1.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vmToMigrate1.getMips())*100;/// Monil
                float metric1 = (float)(vmToMigrate1.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vmToMigrate1.getMips())*100;/// Monil
                float metric2 = (float)(vmToMigrate1.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vmToMigrate1.getMips())*100;/// Monil
                float metric3 = (float)(vmToMigrate1.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vmToMigrate1.getMips())*100;/// Monil
                float metric4 = (float)(vmToMigrate1.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vmToMigrate1.getMips())*100;/// Monil
                float metric5=(float)((metric0+metric1+metric2+metric3+metric4)/5);
                /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vmToMigrate1.getId()+" Ram : "+vmToMigrate1.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil*/
                return vmToMigrate1;                
                }

	}

}
