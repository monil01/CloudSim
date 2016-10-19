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
import java.util.Random;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * The Random Selection (RS) VM selection policy.
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
public class PowerVmSelectionPolicyRandomSelectionWithMigrationControlsd extends PowerVmSelectionPolicy {

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
		int index1 = (new Random()).nextInt(migratableVms.size());
                int index=0;
                Vm vm=null;
                Vm vmToMigrate1=migratableVms.get(index1);
                //System.out.println(migratableVms.isEmpty());
               	while (migratableVms.isEmpty()==false) {
                    	if (migratableVms.isEmpty()) {
			break;
                        }
                        index = (new Random()).nextInt(migratableVms.size());
                        vm=migratableVms.get(index);
                        /*for (Vm vm1 : migratableVms) {
                            System.out.println(" VM ID:"+vm1.getId());
                        }
                        System.out.println(" to be removed:"+vm.getId());
                        System.out.println(" Migratabel VM status:"+migratableVms.size());
                        migratableVms.remove(vm);*/
                        if (vm.isInMigration()) {
			migratableVms.remove(vm);
                        continue;
			}
                        /*float metric5=0;
                        if (CloudSim.clock()>= 2700){
                        //System.out.println("i am in");
                        /*float metric0 = (float)(vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips())*100;/// Monil
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
                        /*System.out.println(CloudSim.clock()+ "HOst Id: "+host.getId() +" host utilization: "+
                                host.getUtilizationOfCpuMips()/host.getTotalMips()*100+" id=" +
                                vm.getId()+" Ram : "+vm.getRam()+"  "+metric0+" "+metric1+" "+metric2+" "+
                                metric3+" "+metric4+" average: "+ metric5);/// Monil 
                        }*/
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
                        }
                        
                        if (metric5>3){
			return migratableVms.get(index);
                        }else{
                        //System.out.println("removed");
                        migratableVms.remove(vm);
			} 

                //System.out.println(vm.toString());
                //System.out.println(vm.getId());
		}

                
                //migratableVms.remove(migratableVms.get(index));
                System.out.println("not found");
		return vmToMigrate1; 
	}

}
