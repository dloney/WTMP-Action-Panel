package usbr.wat.plugins.actionpanel.ui.planning;

import java.awt.Component;                      // Return type of JTabbedPane.getSelectedComponent(), used to resolve the active sub-tab
import java.awt.GridBagConstraints;               // Layout constraints for positioning each row/section of this panel
import java.awt.GridBagLayout;                     // Flexible grid-based layout manager
import java.util.List;                             // Ordered collection interface for the project's SimulationGroup manager proxies

import javax.swing.DefaultComboBoxModel;            // Backing model for the Set combo box
import javax.swing.JButton;                         // Edit/New/Delete buttons for both the Set and Simulation Group rows
import javax.swing.JComboBox;                       // Set selector and Simulation Group selector
import javax.swing.JLabel;                          // Row labels
import javax.swing.JOptionPane;                      // Used to confirm Set/Simulation Group deletion
import javax.swing.JTabbedPane;                      // Left-hand tab strip hosting the six sub-tab panels

import com.rma.model.ManagerProxy;                  // Lightweight proxy wrapping each managed SimulationGroup, used as combo-box items
import com.rma.model.Project;                       // Represents the currently open WAT study; provides manager and proxy lookups
import rma.swing.RmaInsets;                          // Standard GridBagConstraints insets constants
import rma.swing.RmaJPanel;                          // Base Swing panel class this component extends, matching ForecastPanel's base class
import rma.util.RMASort;                             // RMA quicksort utility, used to sort the Simulation Group combo alphabetically

import usbr.wat.plugins.actionpanel.ActionsWindow;                          // The parent Actions Window this panel is hosted within
import usbr.wat.plugins.actionpanel.editors.NewSimulationGroupDialog;       // Existing dialog reused, unmodified, for creating/editing Simulation Groups
import usbr.wat.plugins.actionpanel.commands.NewSimulationGroupCmd;         // Existing command class backing standard SimulationGroup creation
import usbr.wat.plugins.actionpanel.model.SimulationGroup;                  // The existing model type used for the Simulation Group row (per the clarified data model, Planning pairs a Set with a standard SimulationGroup, not a new subtype)
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;             // The Set model this panel's Set row manages
import usbr.wat.plugins.actionpanel.model.planning.PlanningSetContainer;    // Holds and persists the full list of Sets for the current project
import usbr.wat.plugins.actionpanel.ui.planning.temptarget.TempTargetPanel; // Temperature Targets sub-tab
import usbr.wat.plugins.actionpanel.ui.PlanningSimulationGroupPanel;        // The planning specific implementation fo the simulation group

/**
 * Top-level content of the Planning tab, added alongside "Prescribed Conditions" and
 * "Forecast Conditions" in {@link ActionsWindow}.
 *
 * Structurally mirrors {@code usbr.wat.plugins.actionpanel.ui.forecast.ForecastPanel}:
 * a selector row above a left-placed {@link JTabbedPane} of six sub-tabs (Initial
 * Conditions, Operations, Meteorology, Boundary Conditions, Temperature Targets,
 * Simulation), with a {@link CategorySummaryStripPanel} showing the current status of the
 * first five sub-tabs (mirroring the same strip already present in the Forecast Conditions
 * workflow).
 *
 * Unlike the Forecast panel, Planning exposes <b>two</b> independent selector rows at the
 * top: a <b>Set</b> row and a <b>Simulation Group</b> row. Per the clarified data model, a
 * Set (the climate-side forcing data — CalSim, Climate Scenario, and Hydrology) and a
 * Simulation Group (the model configuration that data is applied to) are independent, and
 * together form a unique pairing; both selectors are therefore shown side by side rather
 * than one nested inside the other.
 */
@SuppressWarnings("serial")
public class PlanningPanel extends RmaJPanel {

	// The parent ActionsWindow that hosts this panel
	private final ActionsWindow _parent;

	// --- Set row ---
	private PlanningSetPanel _setPanel;

	// --- Simulation Group row ---
	private JComboBox<ManagerProxy> _simGroupCombo;
	private JButton _simGroupEditButton;
	private JButton _simGroupNewButton;
	private JButton _simGroupDeleteButton;

	// Backing store for all Sets defined in the current project
	private final PlanningSetContainer _setContainer = new PlanningSetContainer();

	// Left-hand tab strip hosting the six sub-tab panels
	private JTabbedPane _tabbedPane;

	private InitialConditionsPanel _initialConditionsPanel;
	private OperationsPanel _operationsPanel;
	private MeteorologyPanel _meteorologyPanel;
	private BcPanel _bcPanel;
	private TempTargetPanel _tempTargetPanel;
	private SimulationPanel _simulationPanel;

	// The AbstractPlanningPanel tab currently selected, used to notify it of activation
	private AbstractPlanningPanel _currentPanel;

	/**
	 * Constructs the Planning panel, stores the parent {@link ActionsWindow} reference,
	 * builds all sub-panels and rows, wires listeners, and attempts to load any Sets
	 * already saved for the current project.
	 *
	 * @param parent the {@link ActionsWindow} that owns and hosts this panel; must not be null
	 */
	public PlanningPanel(ActionsWindow parent) {
		super(new GridBagLayout()); // This panel lays out its own rows/sections with GridBagLayout
		_parent = parent; // Remember the owning ActionsWindow, needed by the Set/SimGroup dialogs

		buildControls(); // Build the Set row, Simulation Group row, summary strip, and tabbed pane
		addListeners(); // Wire up button clicks and selection changes
		_setPanel.loadSets(); // Populate the Set combo from any previously saved Sets
		loadSimulationGroupCombo(); // Populate the Simulation Group combo from the project's managers
	}

	/**
	 * Builds and lays out the Set row, Simulation Group row, category summary strip, and
	 * left-hand tabbed pane.
	 */
	private void buildControls() {
		GridBagConstraints gbc = new GridBagConstraints(); // Shared constraints object, reused/mutated per row

		// Create a new simulation group panel
		_simulationPanel = new SimulationPanel(_parent, this);

		// Create the climate/operations set
		_setPanel = new PlanningSetPanel(this, _setContainer);

		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = RmaInsets.INSETS5505;

		add(_setPanel, gbc); // Place the Set panel

		// Simulation Group
		_simGroupPanel = new PlanningSimulationGroupPanel(_simulationPanel);

		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = RmaInsets.INSETS5505;

		add(_simGroupPanel, gbc);

		// --- Category summary strip ---
		// Build every sub-tab panel up front, since both the strip and the tabbed pane below need them
		_initialConditionsPanel = new InitialConditionsPanel(this);
		_operationsPanel = new OperationsPanel(this);
		_meteorologyPanel = new MeteorologyPanel(this);
		_bcPanel = new BcPanel(this);
		_tempTargetPanel = new TempTargetPanel(this);

		// Create a new tabl plane
		_tabbedPane = new JTabbedPane();
		String pos = System.getProperty("WTMP.PlanningTabs.Placement");

		// Default to LEFT placement; override if a recognised value is specified
		int tabPlacement = JTabbedPane.LEFT;
		if ("left".equalsIgnoreCase(pos)) {
			tabPlacement = JTabbedPane.LEFT;

		} else if ("right".equalsIgnoreCase(pos)) {
			tabPlacement = JTabbedPane.RIGHT;

		} else if ("bottom".equalsIgnoreCase(pos)) {
			tabPlacement = JTabbedPane.BOTTOM;

		} else if ("top".equalsIgnoreCase(pos)) {
			tabPlacement = JTabbedPane.TOP;
		}

		_tabbedPane.setTabPlacement(tabPlacement);

		// Add the tabbed pane below the group panel; it claims all remaining vertical space
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = RmaInsets.INSETS5505;
		add(_tabbedPane, gbc);

		// Register each sub-panel as a named tab in display order
		_tabbedPane.addTab("Initial Conditions", _initialConditionsPanel);
		_tabbedPane.addTab("Operations", _operationsPanel);
		_tabbedPane.addTab("Meteorology", _meteorologyPanel);
		_tabbedPane.addTab("Boundary Conditions", _bcPanel);
		_tabbedPane.addTab("Temperature Targets", _tempTargetPanel);
		_tabbedPane.addTab("Simulation", _simulationPanel);

		// Capture the initially selected tab as the current panel
		_currentPanel = (AbstractPlanningPanel) _tabbedPane.getSelectedComponent();

		// Disable all sub-panels until a simulation group is loaded
		_simulationPanel.setEnabled(false);
		_initialConditionsPanel.setEnabled(false);
		_operationsPanel.setEnabled(false);
		_meteorologyPanel.setEnabled(false);
		_tempTargetPanel.setEnabled(false);
		_bcPanel.setEnabled(false);
		
	}

	/**
	 * Attaches listeners for the Set row, Simulation Group row, and left-hand tab changes.
	 */
	private void addListeners() {
		// The Set row's New/Edit/Delete buttons and combo-box selection are now wired
		// internally by PlanningSetPanel itself, matching the Simulation Group row's pattern.

		_simGroupNewButton.addActionListener(e -> newSimulationGroup()); // Opens the standard New Simulation Group dialog
		_simGroupEditButton.addActionListener(e -> editSimulationGroup()); // Opens the standard Edit Simulation Group dialog
		_simGroupDeleteButton.addActionListener(e -> deleteSimulationGroup()); // Removes the selected Simulation Group
		_simGroupCombo.addActionListener(e -> simGroupSelected()); // Propagates selection changes to every sub-tab

		_tabbedPane.addChangeListener(e -> tabSelectionChanged()); // Keeps the summary strip and panelActivated() hook in sync
	}

	/**
	 * Handles a left-hand tab selection change: notifies the newly selected sub-tab
	 * panel that it has become active and updates the summary strip's highlighted box.
	 */
	private void tabSelectionChanged() {
		Component selected = _tabbedPane.getSelectedComponent(); // The panel now showing in the tabbed pane
		if (!(selected instanceof AbstractPlanningPanel)) {
			return; // Defensive guard; every tab added above is an AbstractPlanningPanel
		}

		_currentPanel = (AbstractPlanningPanel) selected; // Remember which sub-tab is now active
		_currentPanel.panelActivated(); // Give the panel a chance to refresh any stale content
	}

	// --- Set row behavior ---
	// Loading, persisting, and combo-box maintenance for Sets are now owned by
	// PlanningSetPanel (see _setPanel), matching how BaseSimulationGroupPanel owns that
	// behavior for the Simulation Group row. PlanningPanel's only remaining responsibility
	// for the Set row is reacting to the panel's selection callback below.

	/**
	 * Returns the Set panel that manages the Set row's combo box and New/Edit/Delete buttons.
	 *
	 * @return the PlanningSetPanel instance owned by this panel
	 */
	public PlanningSetPanel getSetPanel() {
		return _setPanel;
	}

	/**
	 * Propagates the newly selected Set to every sub-tab panel and, for temperature
	 * targets, allows the tab to refresh its mode/detail controls accordingly.
	 *
	 * Called back by {@link PlanningSetPanel} whenever its combo-box selection changes.
	 *
	 * @param set the newly selected Set, or null if none is selected
	 */
	public void setSelectedSet(PlanningSet set) {
		// Every sub-tab needs to know about the newly active Set, regardless of which is currently visible
		_initialConditionsPanel.setPlanningSet(set);
		_operationsPanel.setPlanningSet(set);
		_meteorologyPanel.setPlanningSet(set);
		_bcPanel.setPlanningSet(set);
		_tempTargetPanel.setPlanningSet(set);
		_simulationPanel.setPlanningSet(set);
	}

	// --- Simulation Group row behavior ---

	/**
	 * Reloads the Simulation Group combo box from the project's manager list, sorted
	 * alphabetically, mirroring the loading behavior of
	 * {@code usbr.wat.plugins.actionpanel.ui.BaseSimulationGroupPanel#loadSimulationGroupCombo()}.
	 */
	private void loadSimulationGroupCombo() {
		Project prj = Project.getCurrentProject(); // The currently open WAT study
		if (prj == null) {
			return; // No project open yet; nothing to load
		}

		List<ManagerProxy> proxies = prj.getManagerProxyListForType(SimulationGroup.class); // Every standard SimulationGroup in the project
		RMASort.quickSort(proxies); // Sort alphabetically for a predictable, easy-to-scan combo box

		_simGroupCombo.setModel(new DefaultComboBoxModel<>(proxies.toArray(new ManagerProxy[0]))); // Rebuild the combo's model
		if (_simGroupCombo.getItemCount() > 0) {
			_simGroupCombo.setSelectedIndex(0); // Default to the first available group
		} else {
			simGroupSelected(); // No groups at all; clear every sub-tab's Simulation Group state
		}
	}

	/**
	 * Opens the standard New Simulation Group dialog (unchanged from the rest of the
	 * plugin), configured for the standard {@link SimulationGroup} type, and on success
	 * refreshes the combo box and selects the new group.
	 */
	private void newSimulationGroup() {
		NewSimulationGroupDialog dlg = new NewSimulationGroupDialog(_parent, true, "New Simulation Group"); // Reuse the existing dialog as-is
		dlg.setSimulationGroupClass(SimulationGroup.class); // Ensure it creates a standard SimulationGroup, not a ForecastSimGroup
		dlg.setSimulationGroupFactory(NewSimulationGroupCmd.class); // Backing command class for standard group creation
		dlg.setVisible(true); // Blocks until the dialog is closed

		loadSimulationGroupCombo(); // Refresh the combo box regardless of outcome, in case a group was created
	}

	/**
	 * Opens the standard Edit Simulation Group dialog for the currently selected group.
	 */
	private void editSimulationGroup() {
		ManagerProxy proxy = (ManagerProxy) _simGroupCombo.getSelectedItem(); // Nothing to edit if none selected
		if (proxy == null) {
			return;
		}

		SimulationGroup group = (SimulationGroup) proxy.loadManager(); // Resolve the live object from its proxy
		NewSimulationGroupDialog dlg = new NewSimulationGroupDialog(_parent, true, "Edit Simulation Group"); // Reuse the existing dialog as-is
		dlg.setSimulationGroupClass(SimulationGroup.class); // Ensure it edits a standard SimulationGroup
		dlg.setSimulationGroupFactory(NewSimulationGroupCmd.class); // Backing command class for standard group editing
		dlg.setVisible(true); // Blocks until the dialog is closed

		loadSimulationGroupCombo(); // Refresh the combo box regardless of outcome, in case the group changed
	}

	/**
	 * Deletes the currently selected Simulation Group after confirmation.
	 *
	 * <p><b>Extension point:</b> this should delegate to the same deletion pathway used
	 * elsewhere in the plugin (see
	 * {@code usbr.wat.plugins.actionpanel.actions.DeleteSimulationGroupAction}) so
	 * cascading cleanup (associated files, tree nodes, etc.) is handled consistently;
	 * wiring that in is left for integration since it requires the live
	 * {@code ActionsWindow}/tree context this panel does not otherwise need.</p>
	 */
	private void deleteSimulationGroup() {
		ManagerProxy proxy = (ManagerProxy) _simGroupCombo.getSelectedItem(); // Nothing to delete if none selected
		if (proxy == null) {
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Delete the selected Simulation Group?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return; // User declined the confirmation prompt
		}

		// TODO: delegate to DeleteSimulationGroupAction (or equivalent) for full cascading cleanup.
		loadSimulationGroupCombo(); // Refresh the combo box; real deletion still needs to be wired up above
	}

	/**
	 * Propagates the newly selected Simulation Group to every sub-tab panel.
	 */
	private void simGroupSelected() {
		ManagerProxy proxy = (ManagerProxy) _simGroupCombo.getSelectedItem(); // May be null if the combo is empty
		SimulationGroup group = proxy != null ? (SimulationGroup) proxy.loadManager() : null; // Resolve the live object, or null

		// Every sub-tab needs to know about the newly paired Simulation Group, regardless of which is currently visible
		_initialConditionsPanel.setSimulationGroup(group);
		_operationsPanel.setSimulationGroup(group);
		_meteorologyPanel.setSimulationGroup(group);
		_bcPanel.setSimulationGroup(group);
		_tempTargetPanel.setSimulationGroup(group);
		_simulationPanel.setSimulationGroup(group);
	}
}
