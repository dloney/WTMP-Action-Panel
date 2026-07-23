package usbr.wat.plugins.actionpanel.commands;

import java.util.List;                                                  // Collections interface used for lists of simulations to include in the new group

import com.rma.io.RmaFile;                                              // Abstraction for a file/directory within the RMA file system utilities
import com.rma.model.Project;                                           // Accessor for the current project and project-level operations

import hec2.wat.model.WatAnalysisPeriod;                                // WAT model type representing the analysis period associated with a simulation group
import hec2.wat.model.WatSimulation;                                    // WAT model type representing a single simulation scenario or run

import usbr.wat.plugins.actionpanel.model.prescribed.PrescribedSimulationGroup;

/**
 * Command for creating a new PrescribedSimulationGroup and populating it with simulations.
 *
 * This command specializes {@link AbstractNewSimulationGroupCmd} for the
 * prescribed conditions workflow. It identifies the manager class, type, and
 * file extension for {@link PrescribedSimulationGroup}, and defaults to running the
 * extract step for copied simulations.
 */
public class NewSimulationGroupCmd extends AbstractNewSimulationGroupCmd {

	/**
	 * Constructs a command to create a new simulation group.
	 *
	 * @param project the project in which the group is created
	 * @param name    the display name of the new group
	 * @param descr   the description of the new group
	 * @param dir     the directory for group-related files
	 * @param ap      the analysis period to set on the group
	 * @param sims    the simulations to copy into the group
	 */
	public NewSimulationGroupCmd(Project project, String name, String descr,
	                             RmaFile dir, WatAnalysisPeriod ap, List<WatSimulation> sims) {
		// Initialize the base command and enable extract for prescribed conditions
		super(project, name, descr, dir, ap, sims, true);

	}

	/**
	 * Returns the file extension used for PrescribedSimulationGroup files.
	 *
	 * @return the file extension associated with PrescribedSimulationGroup
	 */
	@Override
	public String getExtension() {
		// Provide the simulation group file extension
		return PrescribedSimulationGroup.FILE_EXT;
	}

	/**
	 * Returns the fully qualified manager class name for PrescribedSimulationGroup.
	 *
	 * @return the manager class name
	 */
	@Override
	public String getManagerClass() {
		// Identify the manager class handled by this command
		return PrescribedSimulationGroup.class.getName();
	}

	/**
	 * Returns a descriptive manager type string.
	 *
	 * @return a human-readable manager type
	 */
	@Override
	public String getManagerType() {
		// Label this manager type for UI and logging
		return "Simulation Group";
	}

	/**
	 * Returns the created PrescribedSimulationGroup.
	 *
	 * @return the PrescribedSimulationGroup produced by this command
	 */
	@Override
	public PrescribedSimulationGroup getSimulationGroup() {
		// Narrow the base simulation group type to the prescribed conditions type
		return (PrescribedSimulationGroup) super.getSimulationGroup();
	}
}