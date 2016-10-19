/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package power_aware_new;

import java.io.IOException;
//import org.cloudbus.cloudsim.examples.power.planetlab.Dvfs;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabRunner;

/**
 *
 * @author alaul
 */
public class power_aware_new {
           
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		boolean outputToFile = true; // by monil. value made true
		String inputFolder = power_aware_new.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		String workload = "20110303"; // PlanetLab workload
		String vmAllocationPolicy = "iqr"; // Inter Quartile Range (IQR) VM allocation policy
		String vmSelectionPolicy = "migmc"; // migration control with Maximum Correlation (MC) VM selection policy
		String parameter = "1.0"; // the safety parameter of the IQR policy

		new PlanetLabRunner(
				enableOutput,
				outputToFile,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter);
	}

}
