/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;

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
public class PowerVmSelectionPolicyMinimumMigrationTime extends PowerVmSelectionPolicy {

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
                
		double minMetric = Double.MAX_VALUE;
		for (Vm vm : migratableVms) {
			if (vm.isInMigration()) {
				continue;
			}
                        //vm.getId();
                       
                       /* float metric0 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100;/// Monil
                        float metric1 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vm.getMips())*100;/// Monil
                        float metric2 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vm.getMips())*100;/// Monil
                        float metric3 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vm.getMips())*100;/// Monil
                        float metric4 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vm.getMips())*100;/// Monil
                        float metric5=(float)((metric0+metric1+metric2+metric3+metric4)/5);
                        /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil */
                        
                        
			double metric = vm.getRam();
			if (metric < minMetric) {
				minMetric = metric;
				vmToMigrate = vm;
			}
		}
               /* float metric0 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vmToMigrate.getMips())*100;/// Monil
                float metric1 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-300) / vmToMigrate.getMips())*100;/// Monil
                float metric2 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-600) / vmToMigrate.getMips())*100;/// Monil
                float metric3 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-900) / vmToMigrate.getMips())*100;/// Monil
                float metric4 = (float)(vmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()-1200) / vmToMigrate.getMips())*100;/// Monil
                float metric5=(float)((metric0+metric1+metric2+metric3+metric4)/5);
                System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vmToMigrate.getId()+" Ram : "+vmToMigrate.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil */
		return vmToMigrate;
	}

}
