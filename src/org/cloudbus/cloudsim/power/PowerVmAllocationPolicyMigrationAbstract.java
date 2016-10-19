/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabConstants;
import org.cloudbus.cloudsim.power.lists.PowerVmList;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;
import org.cloudbus.cloudsim.examples.power.Constants; 
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * The class of an abstract power-aware VM allocation policy that dynamically optimizes the VM
 * allocation using migration.
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
public abstract class PowerVmAllocationPolicyMigrationAbstract extends PowerVmAllocationPolicyAbstract {

	/** The vm selection policy. */
	private PowerVmSelectionPolicy vmSelectionPolicy;

	/** The saved allocation. */
	private final List<Map<String, Object>> savedAllocation = new ArrayList<Map<String, Object>>();

	/** The utilization history. */
	private final Map<Integer, List<Double>> utilizationHistory = new HashMap<Integer, List<Double>>();

	/** The metric history. */
	private final Map<Integer, List<Double>> metricHistory = new HashMap<Integer, List<Double>>();

	/** The time history. */
	private final Map<Integer, List<Double>> timeHistory = new HashMap<Integer, List<Double>>();

	/** The execution time history vm selection. */
	private final List<Double> executionTimeHistoryVmSelection = new LinkedList<Double>();

	/** The execution time history host selection. */
	private final List<Double> executionTimeHistoryHostSelection = new LinkedList<Double>();

	/** The execution time history vm reallocation. */
	private final List<Double> executionTimeHistoryVmReallocation = new LinkedList<Double>();

	/** The execution time history total. */
	private final List<Double> executionTimeHistoryTotal = new LinkedList<Double>();
        public double numberofunderload=0;
        public double numberofsinglepass=0;
        public double numberofdoublepass=0;
        public double numberofnewactivehost=0;

	/**
	 * Instantiates a new power vm allocation policy migration abstract.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 */

	public PowerVmAllocationPolicyMigrationAbstract(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		super(hostList);
		setVmSelectionPolicy(vmSelectionPolicy);
	}

	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * 
	 * @return the array list< hash map< string, object>>
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		//System.out.println("starts");
                
                numberofunderload=0;
                numberofsinglepass=0;
                numberofdoublepass=0;
                numberofnewactivehost=0;
                //printHostUtilization();
                ExecutionTimeMeasurer.start("optimizeAllocationTotal");

		ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
		setPriorityForVmsSla(); //calls this function for setting the Sla voilation priority
                List<PowerHostUtilizationHistory> overUtilizedHosts = getOverUtilizedHosts();
		getExecutionTimeHistoryHostSelection().add(
				ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

		printOverUtilizedHosts(overUtilizedHosts);
                //System.out.println(" ");
                //System.out.println("Number of Over Utilized hosts: "+ overUtilizedHosts.size());
                List<PowerHost> switchedOffHosts = getSwitchedOffHosts();
                //System.out.println("Number of switched off hosts: "+ switchedOffHosts.size());
                //System.out.println("Number of Over Utilized hosts: "+ PlanetLabConstants.NUMBER_OF_HOSTS);
		saveAllocation();

		ExecutionTimeMeasurer.start("optimizeAllocationVmSelection");
		List<? extends Vm> vmsToMigrate = getVmsToMigrateFromHosts(overUtilizedHosts);
		getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationVmSelection"));

		Log.printLine("Reallocation of VMs from the over-utilized hosts:");
		ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
		List<Map<String, Object>> migrationMap = getNewVmPlacement(vmsToMigrate, new HashSet<Host>(
				overUtilizedHosts));
		getExecutionTimeHistoryVmReallocation().add(
				ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
		Log.printLine();

		migrationMap.addAll(getMigrationMapFromUnderUtilizedHosts(overUtilizedHosts));

		restoreAllocation();

		getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));
		printOverUtilizedHosts(overUtilizedHosts);
                //overUtilizedHosts = getOverUtilizedHosts();
                /*System.out.println("Number of Over Utilized hosts: "+ overUtilizedHosts.size());
                System.out.println("Number of newly put sleeping host "+ numberofunderload);
                System.out.println("Number of newly active host "+ numberofnewactivehost);
                System.out.println("Number of sucessful singlepass "+ numberofsinglepass);
                System.out.println("Number of sucessful doublepass  "+ numberofdoublepass);
                switchedOffHosts = getSwitchedOffHosts();
                System.out.println("Number of switched off hosts after simulation: "+ switchedOffHosts.size());
                //System.out.println("Number of active hosts after simulation: "+ (PlanetLabConstants.NUMBER_OF_HOSTS - switchedOffHosts.size()));
		PlanetLabConstants.NUMBER_OF_singlepass += numberofsinglepass;
                PlanetLabConstants.NUMBER_OF_doublepass += numberofdoublepass;
                //int numberofvm = getVmsnumber();
                //System.out.println("Number of total VM : "+ numberofvm); */
                return migrationMap;
	}

        int getVmsnumber(){
            int numberofvm =0;
            		for (PowerHost host : this.<PowerHost> getHostList()) {
                            numberofvm +=host.getVmList().size();
                        }
            return numberofvm;           
        }
        
        protected int printHostUtilization() {
            System.out.println("");
            /*for (PowerHost host : this.<PowerHost> getHostList()){
                System.out.print(host.getId()+";");
            }*/
            for (PowerHost host : this.<PowerHost> getHostList()){
                System.out.print(host.getUtilizationOfCpu()*100+";");
            }
            System.out.println("");
            for (PowerHost host : this.<PowerHost> getHostList()){
                System.out.print(isHostOverUtilized(host)+";");
            }
            System.out.println("");
            return 0; 
        }
	/**
	 * Sets the priority of VMs for SLA violation.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 */
	protected void setPriorityForVmsSla() {
            
                for (PowerHost host : this.<PowerHost> getHostList()) {
                    
                    for (Vm vm: host.getVmList()) {
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
                                                       
						}
					}
				}
                                //System.out.println("VM ID: "+vm.getId() + " host ID: "+vm.getHost().getId()+" time: "+entry.getTime()+" previous Allocated: "+previousAllocated+" previousRequested: "+previousRequested+" previousTime: "+previousTime+" previousIsInMigration: "+previousIsInMigration);
                                previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime(); 
				previousIsInMigration = entry.isInMigration();
			}
                        
                        if (((vmTotalRequested - vmTotalAllocated) / vmTotalRequested)>= (0.8 * Constants.SLA)) {
                            vm.priority=1;
                        } else {
                            vm.priority-=.1;
                            if (vm.priority<0) vm.priority=0;
                                    }
                        //System.out.println("VM ID "+vm.getId() + "  sla:"+ (vmTotalRequested - vmTotalAllocated) / vmTotalRequested + " Priority: " + vm.priority);
 
                    }
                }        
	}
        
	/**
	 * Gets the migration map from under utilized hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the migration map from under utilized hosts
	 */
	protected List<Map<String, Object>> getMigrationMapFromUnderUtilizedHosts(
			List<PowerHostUtilizationHistory> overUtilizedHosts) {
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		List<PowerHost> switchedOffHosts = getSwitchedOffHosts();

		// over-utilized hosts + hosts that are selected to migrate VMs to from over-utilized hosts
		Set<PowerHost> excludedHostsForFindingUnderUtilizedHost = new HashSet<PowerHost>();
		excludedHostsForFindingUnderUtilizedHost.addAll(overUtilizedHosts);
		excludedHostsForFindingUnderUtilizedHost.addAll(switchedOffHosts);
                //System.out.println(migrationMap.size());
		excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(migrationMap));

		// over-utilized + under-utilized hosts
		Set<PowerHost> excludedHostsForFindingNewVmPlacement = new HashSet<PowerHost>();
		excludedHostsForFindingNewVmPlacement.addAll(overUtilizedHosts);
		excludedHostsForFindingNewVmPlacement.addAll(switchedOffHosts);

		int numberOfHosts = getHostList().size();

		while (true) {
			if (numberOfHosts == excludedHostsForFindingUnderUtilizedHost.size()) {
				break;
			}

			PowerHost underUtilizedHost = getUnderUtilizedHost(excludedHostsForFindingUnderUtilizedHost);
			if (underUtilizedHost == null) {
				break;
			}

			Log.printLine("Under-utilized host: host #" + underUtilizedHost.getId() + "\n");

			excludedHostsForFindingUnderUtilizedHost.add(underUtilizedHost);
			excludedHostsForFindingNewVmPlacement.add(underUtilizedHost);

			List<? extends Vm> vmsToMigrateFromUnderUtilizedHost = getVmsToMigrateFromUnderUtilizedHost(underUtilizedHost);
			if (vmsToMigrateFromUnderUtilizedHost.isEmpty()) {
				continue;
			}

			Log.print("Reallocation of VMs from the under-utilized host: ");
			if (!Log.isDisabled()) {
				for (Vm vm : vmsToMigrateFromUnderUtilizedHost) {
					Log.print(vm.getId() + " ");
				}
			}
			Log.printLine();

			List<Map<String, Object>> newVmPlacement = getNewVmPlacementFromUnderUtilizedHost(
					vmsToMigrateFromUnderUtilizedHost,
					excludedHostsForFindingNewVmPlacement);

			//excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(newVmPlacement));
                        if(newVmPlacement.isEmpty()) {
                            //excludedHostsForFindingNewVmPlacement.remove(underUtilizedHost);
                            migrationMap.addAll(newVmPlacement);
                            Log.printLine();
                        }
                        else {
                            migrationMap.addAll(newVmPlacement);
                            Log.printLine();
                            numberofunderload +=1;
                            numberofsinglepass +=1;
                        }
		}

		return migrationMap;
	}


        
        
	/**
	 * Prints the over utilized hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 */
	protected void printOverUtilizedHosts(List<PowerHostUtilizationHistory> overUtilizedHosts) {
		if (!Log.isDisabled()) {
			Log.printLine("Over-utilized hosts:");
			for (PowerHostUtilizationHistory host : overUtilizedHosts) {
				Log.printLine("Host #" + host.getId());
			}
			Log.printLine();
		}
	}

	/**
	 * Find host for vm.
	 * 
	 * @param vm the vm
	 * @param excludedHosts the excluded hosts
	 * @return the power host
         * Monil
	 */
        
        
	public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
		double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			if (host.isSuitableForVm(vm)) {
				if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				} 
				if (getUtilizationOfCpuMips(host) == 0 && CloudSim.clock()>300.1) {
					continue;
				}
				try {
					double powerAfterAllocation = getPowerAfterAllocation(host, vm);
					if (powerAfterAllocation != -1) {
						double powerDiff = powerAfterAllocation - host.getPower();
						if (powerDiff < minPower) {
							minPower = powerDiff;
							allocatedHost = host;
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return allocatedHost;
	}

        
        	/**
	 * Find host for vm of singlepass.
	 * 
	 * @param vm the vm
	 * @param excludedHosts the excluded hosts
	 * @return the power host
         * Monil
	 */
        
        
	public PowerHost findHostForVmSinglePass(Vm vm, Set<? extends Host> excludedHosts, Host host1) {
		double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
                        if(host1.getId()== host.getId()) continue;
			
                        if (host.isSuitableForVm(vm)) {
				if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				}
				if (getUtilizationOfCpuMips(host) == 0 && CloudSim.clock()>300.1) {
					continue;
				}
                                //System.out.println(isHostOverUtilizedAfterAllocation(host, vm)+" vm1 " + vm.getId() + " host " + host.getId());

                                try {
					double powerAfterAllocation = getPowerAfterAllocation(host, vm);
					if (powerAfterAllocation != -1) {
						double powerDiff = powerAfterAllocation - host.getPower();
						if (powerDiff < minPower) {
							minPower = powerDiff;
							allocatedHost = host;
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return allocatedHost;
	}

                
        	/**
	 * Find host for vm of double pass.
	 * 
	 * @param vm the vm
	 * @param excludedHosts the excluded hosts
	 * @return the power host
         * Monil
	 */
        
        
	public PowerHost findHostForVmDoublePass(Vm vm, Set<? extends Host> excludedHosts, Host host1, Host host2) {
		double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
                        if(host1.getId()== host.getId()) continue;
                        if(host2.getId()== host.getId()) continue;
                        
                        if (host.isSuitableForVm(vm)) {
				if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				}
				if (getUtilizationOfCpuMips(host) == 0 && CloudSim.clock()>300.1) {
					continue;
				}
                                //System.out.println(isHostOverUtilizedAfterAllocation(host, vm)+" vm1 " + vm.getId() + " host " + host.getId());

                                try {
					double powerAfterAllocation = getPowerAfterAllocation(host, vm);
					if (powerAfterAllocation != -1) {
						double powerDiff = powerAfterAllocation - host.getPower();
						if (powerDiff < minPower) {
							minPower = powerDiff;
							allocatedHost = host;
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return allocatedHost;
	}
        
        
        	/**
	 * Find host for vm from the zero utilization hosts.
	 * 
	 * @param vm the vm
	 * @param excludedHosts the excluded hosts
	 * @return the power host
         * Monil
	 */
        
        
	public PowerHost findHostForVmFromSleepingHost(Vm vm, Set<? extends Host> excludedHosts) {
		double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			if (host.isSuitableForVm(vm)) {
				if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				}
				try {
					double powerAfterAllocation = getPowerAfterAllocation(host, vm);
					if (powerAfterAllocation != -1) {
						double powerDiff = powerAfterAllocation - host.getPower();
						if (powerDiff < minPower) {
							minPower = powerDiff;
							allocatedHost = host;
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return allocatedHost;
	}
        
        
	/**
	 * Find host for vm in single pass.
	 * 
	 * @param vm the vm
	 * @param excludedHosts the excluded hosts
	 * @return the power host
         * Monil
	 */
        
        
	protected List<Map<String, Object>> singlePassPlacement(Vm vm, Set<? extends Host> excludedHosts) {
		//double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;
                //Vm vmSinglePass = null;
		List<Map<String, Object>> migrationMapSinglePass = new LinkedList<Map<String, Object>>();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
                        if (getUtilizationOfCpuMips(host)==0) {
				continue;
			}
                        List<? extends Vm> vmlist = host.getVmList();
                        int z=vmlist.size();
                        int[] id = new int[z];
                        int[] user = new int[z];
                        for (int i=0;i<z;i++){
                            Vm vm1=vmlist.get(i);
                            id[i]=vm1.getId();
                            user[i]=vm1.getUserId();
                        }
                        for (int i=0;i<id.length;i++){
                            Vm vm1=host.getVm(id[i], user[i]);
                            if (vm1.isInMigration()== false &&  vm1.getCurrentRequestedMaxMips() < vm.getCurrentRequestedMaxMips() && host.getVmScheduler().getAvailableMips() + vm1.getCurrentRequestedMaxMips() >  vm.getCurrentRequestedTotalMips()) {
                                if (isHostOverUtilizedAfterAllocationSinglePass(host, vm, vm1)==false){
                                    allocatedHost= findHostForVmSinglePass(vm1, excludedHosts, host);
                                    if (allocatedHost != null) {
                                        allocatedHost.vmCreate(vm1);
                                        //System.out.println(" vm " + vm.getId() + " host " + host.getId() + " vm1 " + vm1.getId() + " host " + allocatedHost.getId());
                                        //host.vmDestroy(vm1);
                                        Log.printLine("VM #" + vm1.getId() + " allocated to host #" + allocatedHost.getId());
                                        host.vmCreate(vm);
                                        Log.printLine("VM #" + vm.getId() + " allocated to host #" + host.getId());
                                        Map<String, Object> migrate = new HashMap<String, Object>();
                                        migrate.put("vm", vm1);
                                        migrate.put("host", allocatedHost);
                                        migrationMapSinglePass.add(migrate);
                                        migrate.put("vm", vm);
                                        migrate.put("host", host);
                                        migrationMapSinglePass.add(migrate);
                                        return migrationMapSinglePass;
                                        //System.exit(0);
                                    }
                                }
                            }
                        }
                }
		return migrationMapSinglePass;
	}

        
	/**
	 * Find host for vm in Double pass.
	 * 
	 * @param vm the vm
	 * @param excludedHosts the excluded hosts
	 * @return the power host
         * Monil
	 */
        
        
	protected List<Map<String, Object>> doublePassPlacement(Vm vm, Set<? extends Host> excludedHosts) {
		//double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;
                //Vm vmSinglePass = null;
		List<Map<String, Object>> migrationMapDoublePass = new LinkedList<Map<String, Object>>();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
                        if (getUtilizationOfCpuMips(host)==0) {
				continue;
			}
                        List<? extends Vm> vmlist = host.getVmList();
                        int z=vmlist.size();
                        int[] id = new int[z];
                        int[] user = new int[z];
                        for (int i=0;i<z;i++){
                            Vm vm1=vmlist.get(i);
                            id[i]=vm1.getId();
                            user[i]=vm1.getUserId();
                        }
                        for (int i=0;i<id.length;i++){
                            Vm vm1=vmlist.get(i);
                            /*System.out.println(id.length);
                            System.out.println(i);
                            System.out.println(vm1.getId());
                            System.out.println(vm1.getHost());
                            System.out.println(vm1.getCurrentRequestedMaxMips());
                            System.out.println(vm.getCurrentRequestedMaxMips());
                            System.out.println(host.getVmScheduler().getAvailableMips());
                            System.out.println(vm1.getCurrentRequestedMaxMips());
                            System.out.println(vm.getCurrentRequestedTotalMips());
                            System.out.println(vm1.isInMigration()); */
                            if (vm1.isInMigration()== false &&  vm1.getCurrentRequestedMaxMips() < vm.getCurrentRequestedMaxMips() && host.getVmScheduler().getAvailableMips() + vm1.getCurrentRequestedMaxMips() >  vm.getCurrentRequestedTotalMips()) {
                            //System.out.println(isHostOverUtilizedAfterAllocationSinglePass(host, vm, vm1));
                            if (isHostOverUtilizedAfterAllocationSinglePass(host, vm, vm1)==false){
                            for (PowerHost hostDouble : this.<PowerHost> getHostList()) {
                                if (excludedHosts.contains(host)) {
                                    continue;
                                }
                                if (excludedHosts.contains(hostDouble)) {
                                    continue;
                                }
                                if (getUtilizationOfCpuMips(host)==0) {
                                    continue;
                                }
                                if (getUtilizationOfCpuMips(hostDouble)==0) {
                                    continue;
                                }
                                List<? extends Vm> vmlistDouble = hostDouble.getVmList();
                                int zDouble=vmlistDouble.size();
                                int[] idDouble = new int[zDouble];
                                int[] userDouble = new int[zDouble];
                                for (int iDouble=0;iDouble<zDouble;iDouble++){
                                    Vm vmDouble=vmlistDouble.get(iDouble);
                                    idDouble[iDouble]=vmDouble.getId();
                                    userDouble[iDouble]=vmDouble.getUserId();
                                }
                                for (int iDouble=0;iDouble<idDouble.length;iDouble++){
                                    Vm vmDouble = vmlistDouble.get(iDouble);
                                    //Vm vmDouble=hostDouble.getVm(idDouble[iDouble], userDouble[iDouble]);                            
                                    if (vmDouble.isInMigration()== false && vmDouble.getCurrentRequestedMaxMips() < vm1.getCurrentRequestedMaxMips() && hostDouble.getVmScheduler().getAvailableMips() + vmDouble.getCurrentRequestedMaxMips() >  vm1.getCurrentRequestedTotalMips()) {
                                        if (isHostOverUtilizedAfterAllocationSinglePass(hostDouble, vm1, vmDouble)==false){
                                            allocatedHost= findHostForVmDoublePass(vmDouble, excludedHosts, host, hostDouble);
                                            if (allocatedHost != null) {
                                                allocatedHost.vmCreate(vmDouble);
                                                Log.printLine("VM #" + vmDouble.getId() + " allocated to host #" + allocatedHost.getId());
                                                //System.out.println(" vm " + vm.getId() + " host " + host.getId() + " vm1 " + vm1.getId() + " host " + allocatedHost.getId());
                                                //hostDouble.vmDestroy(vmDouble);
                                                hostDouble.vmCreate(vm1);
                                                Log.printLine("VM #" + vm1.getId() + " allocated to host #" + hostDouble.getId());
                                                //System.out.println(" vm " + vm.getId() + " host " + host.getId() + " vm1 " + vm1.getId() + " host " + allocatedHost.getId());
                                                //host.vmDestroy(vm1);
                                                //Log.printLine("VM #" + vm1.getId() + " allocated to host #" + allocatedHost.getId());
                                                host.vmCreate(vm);
                                                Log.printLine("VM #" + vm.getId() + " allocated to host #" + host.getId());
                                                Map<String, Object> migrate = new HashMap<String, Object>();
                                                migrate.put("vm", vmDouble);
                                                migrate.put("host", allocatedHost);
                                                migrationMapDoublePass.add(migrate);
                                                migrate.put("vm", vm1);
                                                migrate.put("host", hostDouble);
                                                migrationMapDoublePass.add(migrate);
                                                migrate.put("vm", vm);
                                                migrate.put("host", host);
                                                migrationMapDoublePass.add(migrate);
                                                
                                                return migrationMapDoublePass;
                                                //break;
                                                //System.exit(0);
                                            }
                                        }
                                    }
                                }
                            } 
                            }
                            else { /*
                                for (PowerHost hostDouble : this.<PowerHost> getHostList()) {
                                    if (excludedHosts.contains(host)) {
                                        continue;
                                    }
                                    if (excludedHosts.contains(hostDouble)) {
                                        continue;
                                    }
                                    if (getUtilizationOfCpuMips(host)==0) {
                                        continue;
                                    }
                                    if (getUtilizationOfCpuMips(hostDouble)==0) {
                                        continue;
                                    }
                                    List<? extends Vm> vmlistDouble = hostDouble.getVmList();
                                    int zDouble=vmlistDouble.size();
                                    int[] idDouble = new int[zDouble];
                                    int[] userDouble = new int[zDouble];
                                    for (int iDouble=0;iDouble<zDouble;iDouble++){
                                        Vm vmDouble=vmlistDouble.get(iDouble);
                                        idDouble[iDouble]=vmDouble.getId();
                                        userDouble[iDouble]=vmDouble.getUserId();
                                    }
                                    for (int iDouble=0;iDouble<idDouble.length;iDouble++){
                                        Vm vmDouble = vmlistDouble.get(iDouble);
                                        //Vm vmDouble=hostDouble.getVm(idDouble[iDouble], userDouble[iDouble]);                            
                                        if (vmDouble.isInMigration()== false && vmDouble.getCurrentRequestedMaxMips() < vm1.getCurrentRequestedMaxMips() && hostDouble.getVmScheduler().getAvailableMips() + vmDouble.getCurrentRequestedMaxMips() >  vm1.getCurrentRequestedTotalMips() && hostDouble.getVmScheduler().getAvailableMips() + vmDouble.getCurrentRequestedMaxMips() >  vm1.getCurrentRequestedTotalMips()) {
                                        if (isHostOverUtilizedAfterAllocationSinglePass(hostDouble, vm1, vmDouble)==false){
                                            allocatedHost= findHostForVmDoublePass(vmDouble, excludedHosts, host, hostDouble);
                                            if (allocatedHost != null) {
                                                allocatedHost.vmCreate(vmDouble);
                                                Log.printLine("VM #" + vmDouble.getId() + " allocated to host #" + allocatedHost.getId());
                                                //System.out.println(" vm " + vm.getId() + " host " + host.getId() + " vm1 " + vm1.getId() + " host " + allocatedHost.getId());
                                                hostDouble.vmDestroy(vmDouble);
                                                hostDouble.vmCreate(vm1);
                                                Log.printLine("VM #" + vm1.getId() + " allocated to host #" + hostDouble.getId());
                                                //System.out.println(" vm " + vm.getId() + " host " + host.getId() + " vm1 " + vm1.getId() + " host " + allocatedHost.getId());
                                                host.vmDestroy(vm1);
                                                //Log.printLine("VM #" + vm1.getId() + " allocated to host #" + allocatedHost.getId());
                                                host.vmCreate(vm);
                                                Log.printLine("VM #" + vm.getId() + " allocated to host #" + host.getId());
                                                Map<String, Object> migrate = new HashMap<String, Object>();
                                                migrate.put("vm", vmDouble);
                                                migrate.put("host", allocatedHost);
                                                migrationMapDoublePass.add(migrate);
                                                migrate.put("vm", vm1);
                                                migrate.put("host", hostDouble);
                                                migrationMapDoublePass.add(migrate);
                                                migrate.put("vm", vm);
                                                migrate.put("host", host);
                                                migrationMapDoublePass.add(migrate);
                                                //System.exit(0);
                                            }
                                        }
                                        }
                                    }
                                } */                                 
                            }    
                            }
                        }
                    }
                
		return migrationMapDoublePass;
	}

        
        
        
	/**
	 * Checks if is host over utilized after allocation.
	 * 
	 * @param host the host
	 * @param vm the vm
	 * @return true, if is host over utilized after allocation
	 */
	protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
		boolean isHostOverUtilizedAfterAllocation = true;
		if (host.vmCreate(vm)) {
			isHostOverUtilizedAfterAllocation = isHostOverUtilized(host);
			host.vmDestroy(vm);
		}
		return isHostOverUtilizedAfterAllocation;
	}

	/**
	 * Checks if is host over utilized after allocation.
	 * 
	 * @param host the host
	 * @param vm the vm
	 * @return true, if is host over utilized after allocation
	 */
	protected boolean isHostOverUtilizedAfterAllocationSinglePass(PowerHost host1, Vm vm, Vm vm1) {
                PowerHost host=host1;
		boolean isHostOverUtilizedAfterAllocationSinglePass = true;
                //System.out.println(isHostOverUtilized(host)+ "  vm "+vm.getId());
		if (host.vmCreate(vm)) {
                        //System.out.println(isHostOverUtilized(host));
                        host.vmDestroy(vm1);
                        //System.out.println(isHostOverUtilized(host));
			isHostOverUtilizedAfterAllocationSinglePass = isHostOverUtilized(host);
                        //System.out.println(isHostOverUtilized(host));
			host.vmDestroy(vm);
                        //System.out.println(isHostOverUtilized(host));
                        host.vmCreate(vm1);
                        //System.out.println(isHostOverUtilized(host));
		}
		return isHostOverUtilizedAfterAllocationSinglePass;
	}
        
        
        
	/**
	 * Find host for vm.
	 * 
	 * @param vm the vm
	 * @return the power host
	 */
	@Override
	public PowerHost findHostForVm(Vm vm) {
		Set<Host> excludedHosts = new HashSet<Host>();
		if (vm.getHost() != null) {
			excludedHosts.add(vm.getHost());
		}
		return findHostForVm(vm, excludedHosts);
	}

	/**
	 * Extract host list from migration map.
	 * 
	 * @param migrationMap the migration map
	 * @return the list
	 */
	protected List<PowerHost> extractHostListFromMigrationMap(List<Map<String, Object>> migrationMap) {
		List<PowerHost> hosts = new LinkedList<PowerHost>();
		for (Map<String, Object> map : migrationMap) {
			hosts.add((PowerHost) map.get("host"));
		}
		return hosts;
	}


	/**
	 * Gets the new vm placement.
	 * 
	 * @param vmsToMigrate the vms to migrate
	 * @param excludedHosts the excluded hosts
	 * @return the new vm placement,
         * real code is given below of the this function
	 */
	protected List<Map<String, Object>> getNewVmPlacement(
			List<? extends Vm> vmsToMigrate,
			Set<? extends Host> excludedHosts) {
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (Vm vm : vmsToMigrate) {
			PowerHost allocatedHost = findHostForVm(vm, excludedHosts);
			if (allocatedHost != null) {
				allocatedHost.vmCreate(vm);
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
				Map<String, Object> migrate = new HashMap<String, Object>();
				migrate.put("vm", vm);
				migrate.put("host", allocatedHost);
				migrationMap.add(migrate);
			} 
                        else{
                            //monil
                            //System.out.println("starting single pass " + vm.getId());
                            List<Map<String, Object>> migrationMapSinglePass = new LinkedList<Map<String, Object>>();
                            migrationMapSinglePass = singlePassPlacement(vm, excludedHosts);
                            if(!migrationMapSinglePass.isEmpty()) {
                                numberofsinglepass += 1;
                                migrationMap.addAll(migrationMapSinglePass);
                                //System.out.println("Single pass sucess" + vm.getId());
                            } 
                            else{
                                 //System.out.println("starting Double pass " + vm.getId());
                                //List<Map<String, Object>> migrationMapSinglePass = new LinkedList<Map<String, Object>>();
                                migrationMapSinglePass = doublePassPlacement(vm, excludedHosts);
                                if(!migrationMapSinglePass.isEmpty()) {
                                    numberofdoublepass += 1;
                                    migrationMap.addAll(migrationMapSinglePass);
                                    //System.out.println("Double pass sucess" + vm.getId());
                                }
                                else{
                                    
                                    //List<Map<String, Object>> migrationToSleepingHost = new LinkedList<Map<String, Object>>();
                                    //migrationToSleepingHost = findHostForVmFromSleepingHost(vm, excludedHosts);
                                    //System.out.println("single pass over now zero" + vm.getId());
                                    PowerHost allocatedHost1 = findHostForVmFromSleepingHost(vm, excludedHosts);
                                    if (allocatedHost1 != null) {
                                        numberofnewactivehost += 1;
                                        //System.out.println("found destination from zero host " + vm.getId());
                                        allocatedHost1.vmCreate(vm);
                                        Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost1.getId());
                                        Map<String, Object> migrate = new HashMap<String, Object>();
                                        migrate.put("vm", vm);
                                        migrate.put("host", allocatedHost1);
                                        migrationMap.add(migrate);
                                    }
                                }
                            } 
                        }
		}
		return migrationMap;
	}

        
        /*        
	/**
	 * Gets the new vm placement from under utilized host.
	 * 
	 * @param vmsToMigrate the vms to migrate
	 * @param excludedHosts the excluded hosts
	 * @return the new vm placement from under utilized host
	 */   
	protected List<Map<String, Object>> getNewVmPlacementFromUnderUtilizedHost(
			List<? extends Vm> vmsToMigrate,
			Set<? extends Host> excludedHosts) {
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (Vm vm : vmsToMigrate) {
                        if(vm.priority>0){
				Log.printLine("Not all VMs can be reallocated from the host, reallocation cancelled");
				for (Map<String, Object> map : migrationMap) {
					((Host) map.get("host")).vmDestroy((Vm) map.get("vm"));
				}
				migrationMap.clear();
				break;                        
                        }
			PowerHost allocatedHost = findHostForVm(vm, excludedHosts);
			if (allocatedHost != null) {
				allocatedHost.vmCreate(vm);
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
 				Map<String, Object> migrate = new HashMap<String, Object>();
				migrate.put("vm", vm);
				migrate.put("host", allocatedHost);
				migrationMap.add(migrate);
			} 
                        else{
                            List<Map<String, Object>> migrationMapSinglePass = new LinkedList<Map<String, Object>>();
                            migrationMapSinglePass = singlePassPlacement(vm, excludedHosts);
                            if(!migrationMapSinglePass.isEmpty()) {
                                migrationMap.addAll(migrationMapSinglePass);
                                //System.out.println("Single pass sucess" + vm.getId());
                            } 
                            else{
                                 //System.out.println("starting Double pass " + vm.getId());
                                //List<Map<String, Object>> migrationMapSinglePass = new LinkedList<Map<String, Object>>();
                                migrationMapSinglePass = doublePassPlacement(vm, excludedHosts);
                                if(!migrationMapSinglePass.isEmpty()) {
                                    migrationMap.addAll(migrationMapSinglePass);
                                    //System.out.println("Double pass sucess" + vm.getId());
                                }
                                else {
				Log.printLine("Not all VMs can be reallocated from the host, reallocation cancelled");
				for (Map<String, Object> map : migrationMap) {
					((Host) map.get("host")).vmDestroy((Vm) map.get("vm"));
				}
				migrationMap.clear();
				break;
                                }
                            }
                        }
		}
		return migrationMap;
	}

	/**
	 * Gets the vms to migrate from hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the vms to migrate from hosts
	 */
	protected
			List<? extends Vm>
			getVmsToMigrateFromHosts(List<PowerHostUtilizationHistory> overUtilizedHosts) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		for (PowerHostUtilizationHistory host : overUtilizedHosts) {
			while (true) {
				Vm vm = getVmSelectionPolicy().getVmToMigrate(host);
				if (vm == null) {
					break;
				}
				vmsToMigrate.add(vm);
				host.vmDestroy(vm);
				if (!isHostOverUtilized(host)) {
					break;
				}
			}
		}
		return vmsToMigrate;
	}

	/**
	 * Gets the vms to migrate from under utilized host.
	 * 
	 * @param host the host
	 * @return the vms to migrate from under utilized host
	 */
	protected List<? extends Vm> getVmsToMigrateFromUnderUtilizedHost(PowerHost host) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		for (Vm vm : host.getVmList()) {
			if (!vm.isInMigration()) {
				vmsToMigrate.add(vm);
			}
		}
		return vmsToMigrate;
	}

	/**
	 * Gets the over utilized hosts.
	 * 
	 * @return the over utilized hosts
	 */
	protected List<PowerHostUtilizationHistory> getOverUtilizedHosts() {
		List<PowerHostUtilizationHistory> overUtilizedHosts = new LinkedList<PowerHostUtilizationHistory>();
		for (PowerHostUtilizationHistory host : this.<PowerHostUtilizationHistory> getHostList()) {
			if (isHostOverUtilized(host)) {
				overUtilizedHosts.add(host);
			}
		}
		return overUtilizedHosts;
	}

	/**
	 * Gets the switched off host.
	 * 
	 * @return the switched off host
	 */
	protected List<PowerHost> getSwitchedOffHosts() {
		List<PowerHost> switchedOffHosts = new LinkedList<PowerHost>();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.getUtilizationOfCpu() == 0) {
				switchedOffHosts.add(host);
			}
		}
		return switchedOffHosts;
	}

	/**
	 * Gets the under utilized host.
	 * 
	 * @param excludedHosts the excluded hosts
	 * @return the under utilized host
	 */
	protected PowerHost getUnderUtilizedHost(Set<? extends Host> excludedHosts) {
		double minUtilization = 1;
		PowerHost underUtilizedHost = null;
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			double utilization = host.getUtilizationOfCpu();
			if (utilization > 0 && utilization < minUtilization
					&& !areAllVmsMigratingOutOrAnyVmMigratingIn(host)) {
				minUtilization = utilization;
				underUtilizedHost = host;
			}
		}
		return underUtilizedHost;
	}

	/**
	 * Checks whether all vms are in migration.
	 * 
	 * @param host the host
	 * @return true, if successful
	 */
	protected boolean areAllVmsMigratingOutOrAnyVmMigratingIn(PowerHost host) {
		for (PowerVm vm : host.<PowerVm> getVmList()) {
			if (!vm.isInMigration()) {
				return false;
			}
			if (host.getVmsMigratingIn().contains(vm)) {
				return true;
			}
		}
		return true;
	}

	/**
	 * Checks if is host over utilized.
	 * 
	 * @param host the host
	 * @return true, if is host over utilized
	 */
	protected abstract boolean isHostOverUtilized(PowerHost host);

	/**
	 * Adds the history value.
	 * 
	 * @param host the host
	 * @param metric the metric
	 */
	protected void addHistoryEntry(HostDynamicWorkload host, double metric) {
		int hostId = host.getId();
		if (!getTimeHistory().containsKey(hostId)) {
			getTimeHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getUtilizationHistory().containsKey(hostId)) {
			getUtilizationHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getMetricHistory().containsKey(hostId)) {
			getMetricHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getTimeHistory().get(hostId).contains(CloudSim.clock())) {
			getTimeHistory().get(hostId).add(CloudSim.clock());
			getUtilizationHistory().get(hostId).add(host.getUtilizationOfCpu());
			getMetricHistory().get(hostId).add(metric);
		}
	}

	/**
	 * Save allocation.
	 */
	protected void saveAllocation() {
		getSavedAllocation().clear();
		for (Host host : getHostList()) {
			for (Vm vm : host.getVmList()) {
				if (host.getVmsMigratingIn().contains(vm)) {
					continue;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("host", host);
				map.put("vm", vm);
				getSavedAllocation().add(map);
			}
		}
	}

	/**
	 * Restore allocation.
	 */
	protected void restoreAllocation() {
		for (Host host : getHostList()) {
			host.vmDestroyAll();
			host.reallocateMigratingInVms();
		}
		for (Map<String, Object> map : getSavedAllocation()) {
			Vm vm = (Vm) map.get("vm");
			PowerHost host = (PowerHost) map.get("host");
			if (!host.vmCreate(vm)) {
				Log.printLine("Couldn't restore VM #" + vm.getId() + " on host #" + host.getId());
				System.exit(0);
			}
			getVmTable().put(vm.getUid(), host);
		}
	}

	/**
	 * Gets the power after allocation.
	 * 
	 * @param host the host
	 * @param vm the vm
	 * 
	 * @return the power after allocation
	 */
	protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
		double power = 0;
		try {
			power = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the power after allocation. We assume that load is balanced between PEs. The only
	 * restriction is: VM's max MIPS < PE's MIPS
	 * 
	 * @param host the host
	 * @param vm the vm
	 * 
	 * @return the power after allocation
	 */
	protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		double requestedTotalMips = vm.getCurrentRequestedTotalMips();
		double hostUtilizationMips = getUtilizationOfCpuMips(host);
		double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
		double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
		return pePotentialUtilization;
	}
	
	/**
	 * Gets the utilization of the CPU in MIPS for the current potentially allocated VMs.
	 *
	 * @param host the host
	 *
	 * @return the utilization of the CPU in MIPS
	 */
	protected double getUtilizationOfCpuMips(PowerHost host) {
		double hostUtilizationMips = 0;
		for (Vm vm2 : host.getVmList()) {
			if (host.getVmsMigratingIn().contains(vm2)) {
				// calculate additional potential CPU usage of a migrating in VM
				hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2) * 0.9 / 0.1;
			}
			hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
		}
		return hostUtilizationMips;
	}

	/**
	 * Gets the saved allocation.
	 * 
	 * @return the saved allocation
	 */
	protected List<Map<String, Object>> getSavedAllocation() {
		return savedAllocation;
	}

	/**
	 * Sets the vm selection policy.
	 * 
	 * @param vmSelectionPolicy the new vm selection policy
	 */
	protected void setVmSelectionPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
		this.vmSelectionPolicy = vmSelectionPolicy;
	}

	/**
	 * Gets the vm selection policy.
	 * 
	 * @return the vm selection policy
	 */
	protected PowerVmSelectionPolicy getVmSelectionPolicy() {
		return vmSelectionPolicy;
	}

	/**
	 * Gets the utilization history.
	 * 
	 * @return the utilization history
	 */
	public Map<Integer, List<Double>> getUtilizationHistory() {
		return utilizationHistory;
	}

	/**
	 * Gets the metric history.
	 * 
	 * @return the metric history
	 */
	public Map<Integer, List<Double>> getMetricHistory() {
		return metricHistory;
	}

	/**
	 * Gets the time history.
	 * 
	 * @return the time history
	 */
	public Map<Integer, List<Double>> getTimeHistory() {
		return timeHistory;
	}

	/**
	 * Gets the execution time history vm selection.
	 * 
	 * @return the execution time history vm selection
	 */
	public List<Double> getExecutionTimeHistoryVmSelection() {
		return executionTimeHistoryVmSelection;
	}

	/**
	 * Gets the execution time history host selection.
	 * 
	 * @return the execution time history host selection
	 */
	public List<Double> getExecutionTimeHistoryHostSelection() {
		return executionTimeHistoryHostSelection;
	}

	/**
	 * Gets the execution time history vm reallocation.
	 * 
	 * @return the execution time history vm reallocation
	 */
	public List<Double> getExecutionTimeHistoryVmReallocation() {
		return executionTimeHistoryVmReallocation;
	}

	/**
	 * Gets the execution time history total.
	 * 
	 * @return the execution time history total
	 */
	public List<Double> getExecutionTimeHistoryTotal() {
		return executionTimeHistoryTotal;
	}

}
