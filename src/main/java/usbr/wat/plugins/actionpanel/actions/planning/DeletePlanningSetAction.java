package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;   // Event type delivered when a user triggers a bound action (for example, a button press)

import javax.swing.AbstractAction;   // Swing base class for encapsulating an action that can be attached to UI components
import javax.swing.JOptionPane;      // Used to confirm Set deletion

import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;   // The Set model this action deletes
import usbr.wat.plugins.actionpanel.ui.planning.PlanningSetPanel; // Panel that hosts Sets and receives deletion notifications

/**
 * Action that deletes the {@link PlanningSet} currently selected in the owning
 * {@link PlanningSetPanel}, after confirmation.
 *
 * Unlike {@code DeletePlanningSimGroupAction}, which deletes {@code Manager}-backed
 * objects via {@code DeleteManagerFactory} and an {@code ObjectChooser}, a Set is a plain
 * value held in a {@code PlanningSetContainer}; deletion here removes it from that
 * container directly, mirroring the original inline {@code deleteSet()} implementation's
 * confirm-then-remove-then-persist behavior.
 */
public class DeletePlanningSetAction extends AbstractAction {

	/**
	 * Panel that hosts Sets and is notified when the selected Set is deleted.
	 */
	private final PlanningSetPanel _parentPanel;

	/**
	 * Creates the delete-planning-set action with a user-visible name.
	 *
	 * @param parentPanel the panel that will be notified when the selected Set is deleted
	 */
	public DeletePlanningSetAction(PlanningSetPanel parentPanel) {
		// Initialize the action with its display label
		super("Delete...");

		// Store the parent panel reference
		_parentPanel = parentPanel;
	}

	/**
	 * Handles the user-triggered event to delete the currently selected Set.
	 *
	 * Does nothing if no Set is currently selected. Otherwise confirms with the user,
	 * removes the Set from the panel's container, persists the change, and refreshes the
	 * panel's combo box.
	 *
	 * @param e the action event initiating the deletion request
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Nothing to delete if no Set is currently selected
		PlanningSet selected = _parentPanel.getSelectedSet();
		if (selected == null) {
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(_parentPanel,
				"Delete Set \"" + selected.getName() + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return; // User declined the confirmation prompt
		}

		// Remove the Set from the container, persist, and refresh dependent UI
		_parentPanel.setRemoved(selected);
		_parentPanel.saveSetsQuietly();
	}
}
