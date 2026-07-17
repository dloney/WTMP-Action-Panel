package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;   // Event type delivered when a user triggers a bound action (for example, a button press)

import javax.swing.AbstractAction;   // Swing base class for encapsulating an action that can be attached to UI components

import usbr.wat.plugins.actionpanel.ActionPanelPlugin;                      // Plugin entry point used to obtain the Actions window and global context
import usbr.wat.plugins.actionpanel.editors.planning.NewPlanningSetDialog;  // New/Edit dialog for a Set
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;             // The Set model this action edits
import usbr.wat.plugins.actionpanel.ui.PlanningSetPanel;           // Panel that lists and manages Sets within the planning workflow
import usbr.wat.plugins.actionpanel.ui.forecast.SimulationPanel;            // Panel for planning workflows that displays and manages simulations and ensemble sets


/**
 * Action that opens the editor to modify the currently selected {@link PlanningSet}.
 *
 * When invoked, this action constructs and shows the edit dialog pre-populated with the
 * Set currently selected in the owning {@link PlanningSetPanel}. If the user confirms
 * changes, the panel's Set list is persisted and its combo box refreshed to reflect any
 * changes (including a possible rename).
 */
public class EditPlanningSetAction extends AbstractAction {

	/**
	 * Panel that displays and manages Sets within the planning workflow.
	 */
	private final SimulationPanel _parentPanel;

	/**
	 * Creates the edit-planning-set action with a user-visible name.
	 *
	 * @param setPanel the panel whose currently selected Set will be edited
	 */
	public EditPlanningSetAction(SimulationPanel parentPanel) {
		// Set the action's display label used by Swing components
		super("Edit Planning Set...");

		// Store the parent panel reference for later updates
		_parentPanel = parentPanel;
	}

	/**
	 * Handles the user-triggered event to edit planning sets.
	 *
	 * @param e the action event initiating the request
	 */
	public void actionPerformed(ActionEvent e) {
		// Delegate to the core editing workflow
		editPlanningSetAction();
	}

	/**
	 * Opens the planning-set editing dialog and applies user selections to the UI.
	 *
	 * Creates the dialog, populates it with the current forecast simulation group
	 * and the selected simulation, then upon confirmation sets the selected planning
	 * sets back onto the parent panel for display.
	 */
	public void editPlanningSetAction() {
		// Create the edit dialog using the plugin's actions window as the parent
		EditPlanningSetWindow dlg = new EditPlanningSetWindow(ActionPanelPlugin.getInstance().getActionsWindow());

		// Retrieve the active forecast simulation group from the forecast panel
		ForecastSimGroup simGroup = ActionPanelPlugin.getInstance().getActionsWindow().getForecastPanel().getSimulationGroup();

		// Retrieve the currently selected forecast simulation
		WatSimulation simulation = ActionPanelPlugin.getInstance().getActionsWindow().getForecastPanel().getSelectedSimulation();

		// Pre-populate the dialog with the simulation group and selected simulation
		dlg.fillForm(simGroup, simulation);

		// Display the dialog to the user
		dlg.setVisible(true);

		// If the user canceled, do not apply changes
		if (dlg.isCanceled()) {
			return;
		}

		// Fill the parent panel with the planning sets selected/edited in the dialog
		List<PlanningSet> planningSets = simGroup.getPlanningSets(simulation);
		_parentPanel.setPlanningSets(planningSets);
	}
}
