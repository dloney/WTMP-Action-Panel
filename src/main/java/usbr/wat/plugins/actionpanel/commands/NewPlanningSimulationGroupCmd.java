package usbr.wat.plugins.actionpanel.commands;

import java.util.List;                                                              // Collections interface used for lists of simulations to include in the new group

import com.rma.io.RmaFile;                                                          // Abstraction for a file/directory within the RMA file system utilities
import com.rma.model.Project;                                                       // Accessor for the current project and project-level operations

import hec2.wat.model.WatAnalysisPeriod;                                            // WAT model type representing the analysis period associated with a simulation group
import hec2.wat.model.WatSimulation;                                                // WAT model type representing a single simulation scenario or run
import usbr.wat.plugins.actionpanel.model.planning.PlanningSimulationGroup;                // Planning-specific simulation group type used by the planning panel and commands


/**
 * Command for creating a new {@link PlanningSimulationGroup} and populating it with simulations.
 *
 *
 * This command extends {@code AbstractNewSimulationGroupCmd} to tailor creation for
 * planning simulation groups. It supplies the correct manager type, class, and file
 * extension, and defaults to not running extract during creation.
 *
 */
public class NewPlanningSimulationGroupCmd extends AbstractNewSimulationGroupCmd {

	/**
	 * Constructs a command to create a new planning simulation group.
	 *
	 * @param project the project in which the group is created
	 * @param name    the display name of the new planning group
	 * @param descr   the description of the new planning group
	 * @param dir     the directory for group-related files
	 * @param ap      the analysis period to set on the group
	 * @param sims    the simulations to copy into the group
	 */
	public NewPlanningSimulationGroupCmd(Project project, String name, String descr,
	                                     RmaFile dir, WatAnalysisPeriod ap, List<WatSimulation>sims) {
		// Initialize the base command with "runExtract" set to false for planning groups
		super(project, name, descr, dir, ap, sims, false);

	}

	/**
	 * Returns the file extension used for planning simulation group files.
	 *
	 * @return the file extension associated with {@link PlanningSimulationGroup}
	 */
	@Override
	public String getExtension() {
		// Delegate to the planning group's static file extension constant
		return PlanningSimulationGroup.FILE_EXT;
	}

	/**
	 * Returns the fully qualified class name for the planning simulation group manager.
	 *
	 * @return the manager class name
	 */
	@Override
	public String getManagerClass() {
		// Provide the planning simulation group's class name to the framework
		return PlanningSimulationGroup.class.getName();
	}

	/**
	 * Returns the descriptive manager type for this command.
	 *
	 * @return a human-readable manager type string
	 */
	@Override
	public String getManagerType() {
		// Identify this manager type as a Planning Simulation Group
		return "Planning Simulation Group";
	}

	/**
	 * Returns the created planning simulation group.
	 *
	 * @return the {@link PlanningSimulationGroup} produced by this command
	 */
	@Override
	public PlanningSimulationGroup getSimulationGroup() {
		// Narrow the base simulation group type to the planning-specific type
		return (PlanningSimulationGroup) super.getSimulationGroup();
	}

}