//package examples.org.cloudbus.cloudsim.examples.power.planetlab;
package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

/**
 * A simulation of a heterogeneous power aware data center that applies the Inter Quartile Range
 * (IQR) VM allocation policy and Maximum Correlation (MC) VM selection policy.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class Combined {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		boolean outputToFile = true; // by monil. value made true
		String inputFolder = Dvfs.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		String [] workload = {"20110303","20110306","20110309","20110322","20110325","20110403","20110409","20110411","20110412","20110420"}; // PlanetLab workload
		//String workload1="";"20110303","20110306","20110309","20110322","20110325","20110403","20110409","20110411","20110412","20110420"
                String [] vmAllocationPolicy = {"msmd"}; // Inter Quartile Range (IQR) VM allocation policy
		//String vmAllocationPolicy1="msm";,"msmd","iqr","lr","lrr","mad","thr"
                String [] parameter = {"1.2"}; //"iqr","lr","lrr","mad","thr" {"1.5","1.2","1.2","2.5","0.8"};
                String parameter1="";
                String [] vmSelectionPolicy = {"mcsla"}; // Maximum Correlation (MC) VM selection policy // the safety parameter of the IQR policy
                //String vmSelectionPolicy1={"fs","mc","mcmc","mmt","mmtmc","rs","rsmc","mu","mumc"
                int i=0;
                for (String workload1: workload)
                {
                    i=0;
                for (String vmAllocationPolicy1: vmAllocationPolicy)
                {
                    parameter1=parameter[i];
                    i=i+1;
                for (String vmSelectionPolicy1: vmSelectionPolicy)
                {
                System.out.println(workload1+" "+vmAllocationPolicy1+" "+ parameter1 + " "+ vmSelectionPolicy1);
		new PlanetLabRunner(
				enableOutput,
				outputToFile,
				inputFolder,
				outputFolder,
				workload1,
				vmAllocationPolicy1,
				vmSelectionPolicy1,
				parameter1);
                                
                }
                }
                }

                
	}
	

}
