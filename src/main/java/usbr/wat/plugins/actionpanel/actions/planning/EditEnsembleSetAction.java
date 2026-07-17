package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;                                              // Event type delivered when a user triggers a bound action (for example, a button press)

import java.util.List;                                                          // Collections interface used for lists of ensemble sets

import javax.swing.AbstractAction;                                              // Swing base class for encapsulating an action that can be attached to UI components

import hec2.wat.model.WatSimulation;                                            // WAT model type representing a single simulation scenario or run

import usbr.wat.plugins.actionpanel.ActionPanelPlugin;                          // Plugin entry point used to obtain the Actions window and global context
import usbr.wat.plugins.actionpanel.model.planning.EnsembleSet;                 // Planning model representing a group of simulations and its ensemble sets
import usbr.wat.plugins.actionpanel.model.planning.PlanningSimGroup;            // Planning-specific simulation group type used by the planning panel
import usbr.wat.plugins.actionpanel.ui.planning.EditEnsembleSetWindow;          // Dialog window for editing ensemble sets tied to a planning simulation
import usbr.wat.plugins.actionpanel.ui.planning.SimulationPanel;                // Panel for planning workflows that displays and manages simulations and ensemble sets

/**
 * Action that opens the "Edit Ensemble Set" workflow for planning simulations.
 *
 * When invoked, this action:
 * - Creates and shows the ensemble-set editing window
 * - Pre-fills the dialog with the current planning simulation group and selected simulation
 * - Applies user changes back to the parent panel by updating its displayed ensemble sets
 */
public class EditEnsembleSetAction extends AbstractAction {
	/**
	 * Planning simulation panel that owns this action and displays ensemble sets.
	 */
	private final SimulationPanel _parentPanel;

	/**
	 * Creates the edit-ensemble-set action with a user-visible name.
	 *
	 * @param parentPanel the planning simulation panel that will be updated with edited ensemble sets
	 */
	public EditEnsembleSetAction(SimulationPanel parentPanel) {
		// Set the action's display label used by Swing components
		super("Edit Ensemble Set...");

		// Store the parent panel reference for later updates
		_parentPanel = parentPanel;
	}

	/**
	 * Handles the user-triggered event to edit ensemble sets.
	 *
	 * @param e the action event initiating the request
	 */
	public void actionPerformed(ActionEvent e) {
		// Delegate to the core editing workflow
		editEnsembleSetAction();
	}

	/**
	 * Opens the ensemble-set editing dialog and applies user selections to the UI.
	 *
	 * Creates the dialog, populates it with the current planning simulation group
	 * and the selected simulation, then upon confirmation sets the selected ensemble
	 * sets back onto the parent panel for display.
	 */
	public void editEnsembleSetAction() {
		// Create the edit dialog using the plugin's actions window as the parent
		EditEnsembleSetWindow dlg = new EditEnsembleSetWindow(ActionPanelPlugin.getInstance().getActionsWindow());

		// Retrieve the active planning simulation group from the planning panel
		PlanningSimGroup simGroup = ActionPanelPlugin.getInstance().getActionsWindow().getPlanningPanel().getSimulationGroup();

		// Retrieve the currently selected planning simulation
		WatSimulation simulation = ActionPanelPlugin.getInstance().getActionsWindow().getPlanningPanel().getSelectedSimulation();

		// Pre-populate the dialog with the simulation group and selected simulation
		dlg.fillForm(simGroup, simulation);

		// Display the dialog to the user
		dlg.setVisible(true);

		// If the user canceled, do not apply changes
		if (dlg.isCanceled()) {
			return;
		}

		// Fill the parent panel with the ensemble sets selected/edited in the dialog
		List<EnsembleSet> ensembleSets = simGroup.getEnsembleSets(simulation);
		_parentPanel.setEnsembleSets(ensembleSets);
	}
}