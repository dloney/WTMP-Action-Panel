package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;                                                  // Event type delivered when a user triggers a bound action (for example, a button press)

import javax.swing.AbstractAction;                                                  // Swing base class for encapsulating an action that can be attached to UI components

import usbr.wat.plugins.actionpanel.ActionPanelPlugin;                              // Plugin entry point used to obtain the Actions window and global context
import usbr.wat.plugins.actionpanel.commands.NewPlanningSimulationGroupCmd;         // Command class that constructs a new PlanningSimGroup for creation workflows
import usbr.wat.plugins.actionpanel.editors.NewSimulationGroupDialog;               // Dialog used to create or edit a simulation group’s metadata and settings
import usbr.wat.plugins.actionpanel.model.AbstractSimulationGroup;                  // Base type representing a simulation group used by the actions
import usbr.wat.plugins.actionpanel.model.planning.PlanningSimGroup;                // Planning-specific simulation group type used by the planning panel
import usbr.wat.plugins.actionpanel.ui.AbstractSimulationPanel;                     // Base panel type that exposes simulation group operations to the UI
import usbr.wat.plugins.actionpanel.ui.planning.SimulationGroupPanel;                        // UI panel that lists and manages SimulationGroup entries in the planning workflow

/**
 * Action that creates a new planning simulation group.
 *
 * Opens the "New Planning Simulation Group" dialog, configures the class and factory,
 * and upon confirmation, sets the newly created group on the planning panel and
 * adds it to the simulation-group panel list.
 */
public class NewPlanningSimGroupAction extends AbstractAction {
	/**
	 * Panel that displays and manages simulation groups within the planning workflow.
	 */
	private final SimulationGroupPanel _simGroupPanel;

	/**
	 * Parent simulation panel used to provide UI context for this action.
	 */
	private AbstractSimulationPanel _parent;

	/**
	 * Creates the new-planning-simulation-group action with a user-visible name and initial disabled state.
	 *
	 * @param simulationPanel the parent panel providing UI context
	 * @param simGroupPanel   the simulation-group panel that will display the new group
	 */
	public NewPlanningSimGroupAction(AbstractSimulationPanel simulationPanel, SimulationGroupPanel simGroupPanel) {
		// Set the action's display label used by Swing components
		super("New...");

		// Start disabled until the UI logic enables it (for example, when valid context exists)
		setEnabled(false);

		// Store references to the owning panels
		_parent = simulationPanel;

		_simGroupPanel = simGroupPanel;
	}

	/**
	 * Handles the user-triggered event to create a new planning simulation group.
	 *
	 * Constructs and displays the creation dialog, configures the target class
	 * and factory command, and if confirmed, applies the new group to the planning panel
	 * and adds it to the simulation-group panel.
	 *
	 * @param e the action event initiating the request
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Create the dialog for new planning simulation group creation
		NewSimulationGroupDialog dlg = new NewSimulationGroupDialog(
				ActionPanelPlugin.getInstance().getActionsWindow(),
				true,
				"New Planning Simulation Group"
		);

		// Specify the concrete PlanningSimGroup class to be created
		dlg.setSimulationGroupClass(PlanningSimGroup.class);

		// Provide the factory/command used to instantiate and configure the planning group
		dlg.setSimulationGroupFactory(NewPlanningSimulationGroupCmd.class);

		// For plannin workflows, do not run extract automatically on creation
		dlg.setRunExtract(false);

		// Populate default fields and any initial state
		dlg.fillForm();

		// Display the dialog to the user
		dlg.setVisible(true);

		// Abort if the user cancels the dialog
		if (dlg.isCanceled()) {
			return;
		}

		// Retrieve the newly created simulation group from the dialog
		AbstractSimulationGroup sg = dlg.getSimulationGroup();

		// Set the new group as the active selection in the planning panel
		ActionPanelPlugin.getInstance()
				.getActionsWindow()
				.getPlanningPanel()
				.setSimulationGroup((PlanningSimGroup) sg);

		// Add the new group to the UI list and optionally select it
		_simGroupPanel.addSimulationGroup(sg, true);
	}
}