package usbr.wat.plugins.actionpanel.actions.planning;

import java.awt.event.ActionEvent;                                              // Event type delivered when a user triggers a bound action (for example, a button press)

import java.util.List;                                                          // Collections interface used for lists of manager proxies and simulations
import javax.swing.AbstractAction;                                              // Swing base class for encapsulating an action that can be attached to UI components

import com.rma.client.ObjectChooser;                                            // Dialog utility that presents objects for selection or deletion
import com.rma.factories.DeleteManagerFactory;                                  // Factory providing deletion operations for managers and their proxies
import com.rma.model.Manager;                                                   // Base manager type representing a managed model object
import com.rma.model.ManagerProxy;                                              // Proxy wrapper that exposes manager instances and metadata
import com.rma.model.Project;                                                   // Accessor for the current project and project-level operations

import hec2.wat.model.WatSimulation;                                            // WAT model type representing a single simulation scenario or run

import usbr.wat.plugins.actionpanel.ActionPanelPlugin;                          // Plugin entry point used to obtain the Actions window and global context
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;     			// The Set model this action deletes
import usbr.wat.plugins.actionpanel.ui.PlanningSetPanel;   						// Panel that hosts Sets and receives deletion notifications
import usbr.wat.plugins.actionpanel.ui.BasePlanningSetPanel;    				// Base class that gets implemented
import usbr.wat.plugins.actionpanel.ActionsWindow; 								// Import the action windows class
import usbr.wat.plugins.actionpanel.model.AbstractPlanningSet;					// Imports the abstract set class

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
	 * Owning actions window used as the dialog parent and context source.
	 */
	private final ActionsWindow _parent;

	/**
	 * Panel that hosts planning simulation groups and receives deletion notifications.
	 */
	private final BasePlanningSetPanel _parentPanel;

	/**
	 * Creates the delete-planning-set action with a user-visible name.
	 *
	 * @param parentPanel the panel that will be notified when the selected Set is deleted
	 */
	public DeletePlanningSetAction(BasePlanningSetPanel parentPanel, ActionsWindow parent) {
		// Initialize the action with its display label
		super("Delete...");

		// Store the parent window reference
		_parent = parent;

		// Store the parent panel reference
		_parentPanel = parentPanel;
	}

	/**
	 * Handles the user-triggered event to delete selected planning simulation groups.
	 * <p>
	 * Shows an {@code ObjectChooser} in delete mode for {@link PlanningSet} entries,
	 * deletes each selected group and its simulations, notifies the parent panel, and
	 * refreshes the planning panel's group list.
	 *
	 * @param e the action event initiating the deletion request
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Retrieve all manager proxies for PlanningSet from the current project
		List<ManagerProxy> sets = Project.getCurrentProject().getManagerProxyListForType(PlanningSet.class);

		// Create the chooser dialog in delete mode with the available proxies
		ObjectChooser chooser = new ObjectChooser(ActionPanelPlugin.getInstance().getActionsWindow(), true, sets, ObjectChooser.DELETE);

		// Title the chooser appropriately for deletion
		chooser.setTitle("Delete Sets");

		// Display the chooser dialog
		chooser.setVisible(true);

		// Abort if the user cancels the dialog
		if (chooser.isCanceled()) {
			return;
		}

		// Retrieve the selected objects to delete
		Object[] objects = chooser.getSelectedObjects();

		// If nothing was selected, do nothing
		if (objects == null) {
			return;
		}

		// Access the current project (not used below, but retained from original logic)
		Project prj = Project.getCurrentProject();

		// Loop variables for proxy and manager operations
		ManagerProxy proxy = null;
		Manager manager;

		// Iterate over each selected proxy and perform deletion
		for (int i = 0; i < objects.length; i++) {
			// Cast the selected object to a manager proxy
			proxy = (ManagerProxy) objects[i];

			// Load the concrete manager from the proxy
			manager = proxy.loadManager();

			// Only operate on simulation groups
			if (manager instanceof AbstractPlanningSet) {
				// Collect simulations before deleting the group
				List<WatSimulation> sims = ((AbstracPlanningtSet) manager).getSimulations();

				// Delete the group manager first
				DeleteManagerFactory.deleteManager(manager);

				// Delete each simulation manager (iterate backward to avoid index shifting)
				for (int n = sims.size() - 1; n >= 0; n--) {
					DeleteManagerFactory.deleteManager(sims.get(n));
				}
			}
		}

		// Notify the parent panel of the deletion, if available
		if (_parentPanel != null) {
			_parentPanel.setpDeleted(proxy);
		}

		// Refresh the planning panel's simulation group combo/list
		ActionPanelPlugin.getInstance().getActionsWindow().getPlanningPanel().loadsetCombo();

	}
}
