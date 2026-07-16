package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;   // Event type delivered when a user triggers a bound action (for example, a button press)

import javax.swing.AbstractAction;   // Swing base class for encapsulating an action that can be attached to UI components

import usbr.wat.plugins.actionpanel.ActionPanelPlugin;                      // Plugin entry point used to obtain the Actions window and global context
import usbr.wat.plugins.actionpanel.editors.planning.NewPlanningSetDialog;  // New/Edit dialog for a Set
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;             // The Set model this action edits
import usbr.wat.plugins.actionpanel.ui.planning.PlanningSetPanel;           // Panel that lists and manages Sets within the planning workflow

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
	private final PlanningSetPanel _setPanel;

	/**
	 * Creates the edit-planning-set action with a user-visible name.
	 *
	 * @param setPanel the panel whose currently selected Set will be edited
	 */
	public EditPlanningSetAction(PlanningSetPanel setPanel) {
		// Set the action's display label used by Swing components
		super("Edit...");

		// Store the reference to the owning panel
		_setPanel = setPanel;
	}

	/**
	 * Handles the user-triggered event to edit the currently selected Set.
	 *
	 * Does nothing if no Set is currently selected. Otherwise constructs the dialog
	 * pre-populated with the selected Set, shows it, and if confirmed, persists the Set
	 * list and re-selects the (possibly renamed) Set.
	 *
	 * @param e the action event initiating the request
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Nothing to edit if no Set is currently selected
		PlanningSet selected = _setPanel.getSelectedSet();
		if (selected == null) {
			return;
		}

		// Create the dialog, pre-populated with the selected Set
		NewPlanningSetDialog dlg = new NewPlanningSetDialog(
				ActionPanelPlugin.getInstance().getActionsWindow(), selected);

		// Display the dialog to the user; blocks until it is closed
		dlg.setVisible(true);

		// Abort if the user cancels the dialog
		if (dlg.isCanceled()) {
			return;
		}

		// Persist the edited Set list, then refresh and re-select the (possibly renamed) Set;
		// addSet is a no-op on the container since the edit happened in place, but its
		// refresh-and-select behavior is exactly what is needed here.
		_setPanel.saveSetsQuietly();
		_setPanel.addSet(dlg.getPlanningSet(), true);
	}
}
