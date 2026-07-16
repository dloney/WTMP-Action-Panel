package usbr.wat.plugins.actionpanel.ui.planning;

import java.awt.GridBagConstraints;  // Layout constraints for positioning each control in this panel's toolbar row
import java.awt.GridBagLayout;       // Flexible grid-based layout manager used for the toolbar row
import java.awt.event.ItemEvent;     // Carries combo-box selection change data; used to filter DESELECTED events

import javax.swing.Action;           // Swing Action interface returned by the three action factory methods
import javax.swing.JButton;          // Edit/New/Delete buttons
import javax.swing.JComboBox;        // Set selector
import javax.swing.JLabel;           // "Set:" row label

import rma.swing.EnabledJPanel;      // RMA JPanel subclass with built-in enabled/disabled visual state support
import rma.swing.RmaInsets;          // Constants for common GridBagLayout inset configurations

import usbr.wat.plugins.actionpanel.actions.planning.DeletePlanningSetAction; // Action that deletes the selected Set
import usbr.wat.plugins.actionpanel.actions.planning.EditPlanningSetAction;   // Action that opens the editor for the selected Set
import usbr.wat.plugins.actionpanel.actions.planning.NewPlanningSetAction;    // Action that opens the creation dialog for a new Set
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;               // The Set model this panel manages
import usbr.wat.plugins.actionpanel.model.planning.PlanningSetContainer;      // Holds and persists the full list of Sets for the current project


/**
 * Toolbar panel for creating, editing, deleting, and selecting a {@link PlanningSet}.
 *
 * This class is structured to match {@code usbr.wat.plugins.actionpanel.ui.PlanningSimulationGroupPanel}:
 * a single labeled combo box row, Edit/New/Delete buttons whose {@link Action}s are supplied by
 * dedicated factory methods, and a combo-box selection handler that filters out transient
 * DESELECTED events before notifying the owning panel of the new selection.
 *
 * Unlike {@code PlanningSimulationGroupPanel}, this panel does not manage a project
 * {@code Manager}/{@code ManagerProxy}-backed type; Sets are held and persisted directly by a
 * {@link PlanningSetContainer} owned by the parent {@link PlanningPanel}. Aside from that
 * difference in backing store, the control layout and the New/Edit/Delete/selection wiring
 * mirror {@code PlanningSimulationGroupPanel} exactly.
 *
 * When a Set is selected from the combo box, this panel notifies the parent {@link PlanningPanel}
 * via {@link PlanningPanel#setSelectedSet(PlanningSet)}, which also enables or disables the
 * Edit button accordingly. A DESELECTED event with a non-null selection explicitly clears the
 * parent panel by calling {@code setSelectedSet(null)} before the new selection is processed,
 * matching {@code PlanningSimulationGroupPanel}'s handling of combo transitions.
 */
@SuppressWarnings("serial")
public class PlanningSetPanel extends EnabledJPanel {

	/**
	 * The parent Planning panel that owns this toolbar and receives selection notifications.
	 */
	private final PlanningPanel _parent;

	/**
	 * Backing store for all Sets defined in the current project; owned by the parent panel
	 * and shared with it so both can read and persist the same list.
	 */
	private final PlanningSetContainer _setContainer;

	/**
	 * Combo box listing every Set currently held in {@link #_setContainer}.
	 * Protected so the panel's own listeners and helper methods can read the selection directly.
	 */
	protected JComboBox<PlanningSet> _setCombo;

	/**
	 * Button that opens the edit dialog for the selected Set.
	 * Protected so its enabled state can be adjusted from {@link #fillForm(PlanningSet)}.
	 */
	protected JButton _editButton;

	/**
	 * Button that opens the new Set creation dialog.
	 */
	private JButton _newButton;

	/**
	 * Button that deletes the currently selected Set.
	 */
	private JButton _deleteButton;

	/**
	 * Label for the Set combo box.
	 */
	private JLabel _setLabel;


	/**
	 * Constructs the panel, wires it to the given {@link PlanningPanel} as its parent, and
	 * builds and populates all child controls.
	 *
	 * @param parent       the PlanningPanel that owns this toolbar; must not be null
	 * @param setContainer the shared container of Sets for the current project; must not be null
	 */
	public PlanningSetPanel(PlanningPanel parent, PlanningSetContainer setContainer) {
		super(new GridBagLayout());

		_parent = parent;
		_setContainer = setContainer;

		buildControls();
		addListeners();
	}


	/**
	 * Constructs and lays out all child components using GridBagLayout.
	 *
	 * The layout order from left to right is: "Set:" label, combo box, Edit button, New
	 * button, Delete button. The Edit button starts disabled until a Set is selected.
	 */
	protected void buildControls() {
		// --- "Set:" label ---
		_setLabel = new JLabel("Set:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = RmaInsets.INSETS5505;
		add(_setLabel, gbc);

		// --- Set combo box (expands horizontally to fill available space) ---
		_setCombo = new JComboBox<>();
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = RmaInsets.INSETS5505;
		add(_setCombo, gbc);

		// --- Edit button: disabled until a Set is selected ---
		_editButton = new JButton(getEditSetAction());
		_editButton.setEnabled(false);
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = RmaInsets.INSETS5500;
		add(_editButton, gbc);

		// --- New button ---
		_newButton = new JButton(getNewSetAction());
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = RmaInsets.INSETS5500;
		add(_newButton, gbc);

		// --- Delete button: spans to the end of the row ---
		_deleteButton = new JButton(getDeleteSetAction(this));
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = RmaInsets.INSETS5505;
		add(_deleteButton, gbc);
	}


	/**
	 * Attaches the combo-box selection listener.
	 *
	 * The combo-box item listener delegates to {@link #setSelected(ItemEvent)} so the
	 * DESELECTED-filtering logic lives in one place, matching
	 * {@code PlanningSimulationGroupPanel.simGroupSelected(ItemEvent)}.
	 */
	protected void addListeners() {
		_setCombo.addItemListener(this::setSelected);
	}


	/**
	 * Returns the Action bound to the Delete button for Sets.
	 *
	 * The panel itself is passed to the action so it can call back into
	 * {@link #setRemoved(PlanningSet)} after a deletion is confirmed and persisted.
	 *
	 * @param parent this panel instance, provided to the action for post-deletion callbacks
	 * @return a DeletePlanningSetAction configured with this panel
	 */
	protected Action getDeleteSetAction(PlanningSetPanel parent) {
		return new DeletePlanningSetAction(parent);
	}


	/**
	 * Returns the Action bound to the New button for creating Sets.
	 *
	 * @return a NewPlanningSetAction configured with this panel
	 */
	protected Action getNewSetAction() {
		return new NewPlanningSetAction(this);
	}


	/**
	 * Returns the Action bound to the Edit button for editing the selected Set.
	 *
	 * @return a EditPlanningSetAction configured with this panel
	 */
	protected Action getEditSetAction() {
		return new EditPlanningSetAction(this);
	}


	/**
	 * Responds to a combo-box item selection event by resolving the selected Set and
	 * updating the parent PlanningPanel.
	 *
	 * The DESELECTED event is handled specially: if a Set is currently selected when the
	 * deselect fires, the parent panel is cleared immediately by calling
	 * {@code setSelectedSet(null)} before the subsequent SELECTED event arrives. This
	 * prevents momentary display of stale Set data during a transition.
	 *
	 * @param e the item event from the combo box; null is never passed by this panel's own
	 *          listener registration, but is tolerated for symmetry with the base pattern
	 */
	protected void setSelected(ItemEvent e) {
		PlanningSet selected = (PlanningSet) _setCombo.getSelectedItem();

		// On DESELECTED with an active selection, clear the parent panel immediately
		if (e != null && ItemEvent.DESELECTED == e.getStateChange() && selected != null) {
			fillForm(null);
			return;
		}

		fillForm(selected);
	}


	/**
	 * Updates the Edit button state and notifies the parent PlanningPanel of the newly
	 * selected Set.
	 *
	 * @param set the Set to display; null clears the parent panel
	 */
	private void fillForm(PlanningSet set) {
		// Enable the Edit button only when a valid Set is currently selected
		_editButton.setEnabled(set != null);

		// Notify the parent panel to refresh every sub-tab's Set-dependent state
		_parent.setSelectedSet(set);
	}


	/**
	 * Returns the Set currently selected in the combo box, or null if none is selected.
	 *
	 * @return the currently selected PlanningSet, or null
	 */
	public PlanningSet getSelectedSet() {
		return (PlanningSet) _setCombo.getSelectedItem();
	}


	/**
	 * Returns the shared container backing this panel's combo box.
	 *
	 * @return the PlanningSetContainer supplied at construction
	 */
	public PlanningSetContainer getSetContainer() {
		return _setContainer;
	}


	/**
	 * Loads all Sets previously saved for the current project into the combo box.
	 * If loading fails (e.g. malformed XML), the combo box is simply left empty; the user
	 * can still create new Sets.
	 */
	public void loadSets() {
		try {
			_setContainer.load(); // Attempt to read any previously saved Sets from disk
		} catch (Exception ex) {
			// No previously saved Sets, or the file could not be parsed; start empty.
		}
		refreshSetCombo(); // Reflect whatever was (or wasn't) loaded in the combo box
	}


	/**
	 * Reloads the combo box's model from the current contents of {@link #_setContainer}.
	 *
	 * If the previously selected Set is still present it is reselected; otherwise the first
	 * available Set is selected, or, if none exist, the parent panel is cleared.
	 */
	public void refreshSetCombo() {
		Object previouslySelected = _setCombo.getSelectedItem(); // Remember the prior selection, if any

		_setCombo.removeAllItems();
		for (PlanningSet set : _setContainer.getSets()) {
			_setCombo.addItem(set);
		}

		if (_setContainer.getSets().contains(previouslySelected)) {
			_setCombo.setSelectedItem(previouslySelected); // Keep the same Set selected if it still exists
		} else if (_setCombo.getItemCount() > 0) {
			_setCombo.setSelectedIndex(0); // Otherwise default to the first available Set
		} else {
			setSelected(null); // No Sets at all; clear every sub-tab's state
		}
	}


	/**
	 * Persists the current Set list, silently ignoring failures so a save error does not
	 * block the user from continuing to work.
	 *
	 * TODO: surface save failures to the user (e.g. a status bar message) rather than
	 * swallowing them, mirroring the original inline implementation's caveat.
	 */
	public void saveSetsQuietly() {
		try {
			_setContainer.save(); // Write the current Set list out to disk
		} catch (Exception ex) {
			// TODO: surface save failures to the user (e.g. a status bar message) rather than swallowing them.
		}
	}


	/**
	 * Adds the given Set to the combo box and optionally selects it.
	 *
	 * Typically called after a "New" action successfully creates a Set so the combo box
	 * reflects the addition without requiring a full reload.
	 *
	 * @param set        the newly created Set to add; may be null (no-op)
	 * @param selectSet  true to select the Set immediately after adding it
	 */
	public void addSet(PlanningSet set, boolean selectSet) {
		if (set == null) {
			return;
		}

		if (!_setContainer.getSets().contains(set)) {
			_setContainer.addSet(set);
		}

		refreshSetCombo();

		if (selectSet) {
			_setCombo.setSelectedItem(set);
		}
	}


	/**
	 * Removes the given Set from the container and combo box after a deletion has been
	 * confirmed and persisted.
	 *
	 * If the deleted Set was the currently selected item the selection is cleared by
	 * calling {@code setSelectedSet(null)} via {@link #fillForm(PlanningSet)}, matching
	 * {@code PlanningSimulationGroupPanel.simulationGroupDeleted(ManagerProxy)}.
	 *
	 * @param set the Set that was deleted; may be null (no-op)
	 */
	public void setRemoved(PlanningSet set) {
		if (set == null) {
			return;
		}

		Object selected = _setCombo.getSelectedItem();

		_setContainer.removeSetByName(set.getName());
		refreshSetCombo();

		if (selected == set) {
			fillForm(null);
		}
	}

}
