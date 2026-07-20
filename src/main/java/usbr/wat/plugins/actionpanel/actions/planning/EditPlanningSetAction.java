package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;                                                  // Event type delivered when a user triggers a bound action (for example, a button press)

import javax.swing.AbstractAction;                                                  // Swing base class for encapsulating an action that can be attached to UI components

import usbr.wat.plugins.actionpanel.ActionPanelPlugin;                              // Plugin entry point used to obtain the Actions window and global context
import usbr.wat.plugins.actionpanel.editors.NewSimulationGroupDialog;               // Dialog used to create or edit a simulation group's metadata and settings
import usbr.wat.plugins.actionpanel.model.AbstractSimulationGroup;                  // Base type representing a simulation group used by the actions
import usbr.wat.plugins.actionpanel.model.SimulationGroup;                          // Concrete type representing a simulation group (import present even if unused directly here)
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;                // Planning-specific simulation group type used by the planning panel

/**
 * Action that opens the editor to modify a planning {@link PlanningSet}.
 *
 * When invoked, this action constructs and shows the edit dialog pre-populated
 * with the current planning simulation group. If the user confirms changes,
 * the edited group is applied back to the planning panel.
 */
public class EditPlanningSetAction extends AbstractAction {
	/**
	 * Creates the edit-planning-simulation-group action with a user-visible name.
	 */
	public EditPlanningSetAction() {
		// Set the action's display label used by Swing components
		super("Edit...");
	}

	/**
	 * Handles the user-triggered event to edit the current planning simulation group.
	 *
	 * Constructs the edit dialog, fills it with the existing group, shows it,
	 * and if confirmed, applies the changes back to the planning panel.
	 *
	 * @param e the action event initiating the request
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Create the dialog in edit mode with a specific title
		NewSimulationGroupDialog dlg = new NewSimulationGroupDialog(
				ActionPanelPlugin.getInstance().getActionsWindow(),
				true,
				"Edit Simulation Group"
		);

		// Retrieve the current planning simulation group from the planning panel
		PlanningSet planningSet = ActionPanelPlugin.getInstance()
				.getActionsWindow()
				.getPlanningPanel()
				.getSimulationGroup();

		// Pre-populate the dialog fields with the existing group data
		dlg.fillForm(planningSet);

		// Display the dialog to the user
		dlg.setVisible(true);

		// If the user cancels, do not apply any changes
		if (dlg.isCanceled()) {
			return;
		}

		// Get the updated simulation group from the dialog
		AbstractSimulationGroup sg = dlg.getSimulationGroup();

		// Apply the updated group back to the planning panel
		ActionPanelPlugin.getInstance()
				.getActionsWindow()
				.getPlanningPanel()
				.setSimulationGroup((PlanningSet) sg);
	}
}