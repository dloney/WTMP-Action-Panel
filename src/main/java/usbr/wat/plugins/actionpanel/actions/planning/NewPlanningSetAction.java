package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;   // Event type delivered when a user triggers a bound action (for example, a button press)

import javax.swing.AbstractAction;   // Swing base class for encapsulating an action that can be attached to UI components

import usbr.wat.plugins.actionpanel.ActionPanelPlugin;                      // Plugin entry point used to obtain the Actions window and global context
import usbr.wat.plugins.actionpanel.editors.planning.NewPlanningSetDialog;  // New/Edit dialog for a Set
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;             // The Set model this action creates
import usbr.wat.plugins.actionpanel.ui.planning.PlanningSetPanel;           // Panel that lists and manages Sets within the planning workflow

/**
 * Action that creates a new {@link PlanningSet}.
 *
 * Opens the "New Planning Set" dialog and, upon confirmation, adds the resulting Set to
 * the panel's container, persists the updated Set list, and selects the new Set.
 */
public class NewPlanningSetAction extends AbstractAction {

	/**
	 * Panel that displays and manages Sets within the planning workflow.
	 */
	private final PlanningSetPanel _setPanel;

	/**
	 * Creates the new-planning-set action with a user-visible name.
	 *
	 * @param setPanel the panel that will display and persist the new Set
	 */
	public NewPlanningSetAction(PlanningSetPanel setPanel) {
		// Set the action's display label used by Swing components
		super("New...");

		// Store the reference to the owning panel
		_setPanel = setPanel;
	}

	/**
	 * Handles the user-triggered event to create a new Set.
	 *
	 * Constructs and displays the creation dialog and, if confirmed, registers the new Set
	 * with the panel's container, persists the change, and selects it.
	 *
	 * @param e the action event initiating the request
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Create the dialog, modal over the ActionsWindow, for a brand-new Set
		NewPlanningSetDialog dlg = new NewPlanningSetDialog(
				ActionPanelPlugin.getInstance().getActionsWindow());

		// Display the dialog to the user; blocks until it is closed
		dlg.setVisible(true);

		// Abort if the user cancels the dialog
		if (dlg.isCanceled()) {
			return;
		}

		// Retrieve the newly created Set from the dialog
		PlanningSet set = dlg.getPlanningSet();

		// Register it with the panel's container, persist, and select it
		_setPanel.addSet(set, true);
		_setPanel.saveSetsQuietly();
	}
}
