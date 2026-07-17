package usbr.wat.plugins.actionpanel.ui.planning;

import java.awt.Component;                      // Return type of JTabbedPane.getSelectedComponent(), used to resolve the active sub-tab
import java.awt.GridBagConstraints;               // Layout constraints for positioning each row/section of this panel
import java.awt.GridBagLayout;                     // Flexible grid-based layout manager
import java.util.List;                             // Ordered collection interface for the project's SimulationGroup manager proxies

import javax.swing.DefaultComboBoxModel;            // Backing model for the Set combo box
import javax.swing.JButton;                         // Edit/New/Delete buttons for both the Set and Simulation Group rows
import javax.swing.JComboBox;                       // Set selector and Simulation Group selector
import javax.swing.JLabel;                          // Row labels
import javax.swing.JOptionPane;                     // Used to confirm Set/Simulation Group deletion
import javax.swing.JTabbedPane;                     // Left-hand tab strip hosting the six sub-tab panels

import hec2.wat.model.WatSimulation;                // Provides WatSimulation for representing the currently selected WAT simulation

import com.rma.model.ManagerProxy;                  // Lightweight proxy wrapping each managed SimulationGroup, used as combo-box items
import com.rma.model.Project;                       // Represents the currently open WAT study; provides manager and proxy lookups
import rma.swing.RmaInsets;                         // Standard GridBagConstraints insets constants
import rma.swing.RmaJPanel;                         // Base Swing panel class this component extends, matching ForecastPanel's base class

import usbr.wat.plugins.actionpanel.ActionsWindow;                          // The parent Actions Window this panel is hosted within
import usbr.wat.plugins.actionpanel.model.ResultsData;                      // Provides ResultsData for returning the list of selected simulation results
import usbr.wat.plugins.actionpanel.editors.NewSimulationGroupDialog;       // Existing dialog reused, unmodified, for creating/editing Simulation Groups
import usbr.wat.plugins.actionpanel.commands.NewSimulationGroupCmd;         // Existing command class backing standard SimulationGroup creation
import usbr.wat.plugins.actionpanel.model.SimulationGroup;                  // The existing model type used for the Simulation Group row (per the clarified data model, Planning pairs a Set with a standard SimulationGroup, not a new subtype)
import usbr.wat.plugins.actionpanel.model.planning.PlanningSet;             // The Set model this panel's Set row manages
import usbr.wat.plugins.actionpanel.model.planning.PlanningSetContainer;    // Holds and persists the full list of Sets for the current project
import usbr.wat.plugins.actionpanel.ui.planning.temptarget.TempTargetPanel; // Temperature Targets sub-tab
import usbr.wat.plugins.actionpanel.ui.PlanningSimulationGroupPanel;        // The planning specific implementation fo the simulation group
import usbr.wat.plugins.actionpanel.ui.PlanningSetPanel;					// The planning specific implmentation of alternative sets
import usbr.wat.plugins.actionpanel.model.planning.PlanningSimGroup;        // Provides PlanninSimGroup as the top-level data container for all planning data

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

	// The parent ActionsWindow that hosts this planning panel
	private ActionsWindow _parent;

	// The simulation group selection panel displayed above the tabbed pane
	private PlanningSimulationGroupPanel _simGroupPanel;

	// Create the alternative set group
	private PlanningSetPanel _setPanel;

	// The tabbed pane containing all six planning sub-panel tabs
	private JTabbedPane _tabbedPane;

	private InitialConditionsPanel _initialConditionsPanel;				// The Initial Conditions tab panel
	private OperationsPanel _operationsPanel;							// The Operations tab panel
	private MeteorologyPanel _meteorologyPanel;									// The Meteorology tab panel
	private BcPanel _bcPanel;											// The Boundary Conditions tab panel
	private TempTargetPanel _tempTargetsPanel;							// The Temperature Targets tab panel
	private SimulationPanel _simulationPanel;							// The Simulation tab panel
	private PlanningSimGroup _simGroup;									// The currently active planning simulation group; null when no group is selected

	// The AbstractPlanningPanel tab that is currently selected; used to save state on tab switch
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
		//_setPanel.loadSets(); // Populate the Set combo from any previously saved Sets
		//loadSimulationGroupCombo(); // Populate the Simulation Group combo from the project's managers
	}

	/**
	 * Builds and lays out the Set row, Simulation Group row, category summary strip, and
	 * left-hand tabbed pane.
	 */
	private void buildControls() {
		GridBagConstraints gbc = new GridBagConstraints(); // Shared constraints object, reused/mutated per row

		// Create a new group subpanel
		_simulationPanel = new SimulationPanel(_parent, this);
		_initialConditionsPanel = new InitialConditionsPanel(this);
		_operationsPanel = new OperationsPanel(this);
		_meteorologyPanel = new MeteorologyPanel(this);
		_bcPanel = new BcPanel(this);
		_tempTargetsPanel = new TempTargetPanel(this);

		// Disable all sub-panels until a simulation group is loaded
		_simulationPanel.setEnabled(false);
		_initialConditionsPanel.setEnabled(false);
		_operationsPanel.setEnabled(false);
		_meteorologyPanel.setEnabled(false);
		_tempTargetsPanel.setEnabled(false);
		_bcPanel.setEnabled(false);

		// Create the climate/operations set
		_setPanel = new PlanningSetPanel(_simulationPanel);

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
		_tabbedPane.addTab("Temperature Targets", _tempTargetsPanel);
		_tabbedPane.addTab("Simulation", _simulationPanel);

		// Capture the initially selected tab as the current panel
		_currentPanel = (AbstractPlanningPanel) _tabbedPane.getSelectedComponent();

	}

	/**
	 * Attaches listeners for the Set row, Simulation Group row, and left-hand tab changes.
	 */
	private void addListeners() {
		// The Set row's New/Edit/Delete buttons and combo-box selection are now wired
		// internally by PlanningSetPanel itself, matching the Simulation Group row's pattern.

		//_simGroupNewButton.addActionListener(e -> newSimulationGroup()); // Opens the standard New Simulation Group dialog
		//_simGroupEditButton.addActionListener(e -> editSimulationGroup()); // Opens the standard Edit Simulation Group dialog
		//_simGroupDeleteButton.addActionListener(e -> deleteSimulationGroup()); // Removes the selected Simulation Group
		//_simGroupCombo.addActionListener(e -> simGroupSelected()); // Propagates selection changes to every sub-tab

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
	 * Returns the currently active {@link PlanningSimGroup}, or {@code null} if no
	 * simulation group has been selected.
	 *
	 * @return the active {@link PlanningSimGroup}, or {@code null}
	 */
	public PlanningSimGroup getSimulationGroup() {
		return _simGroup;
	}

	/**
	 * Sets the active {@link PlanningSimGroup} and propagates it to all sub-panels.
	 *
	 * If {@code fsg} is non-null, all sub-panels are populated with its data. If
	 * {@code fsg} is {@code null}, all sub-panels are cleared and
	 * {@link #clearPanel()} is called to reset all lower-panel controls.
	 *
	 * @param fsg the {@link PlanningSimGroup} to display, or {@code null} to clear
	 *            all panels
	 */
	public void setSimulationGroup(PlanningSimGroup fsg) {
		_simGroup = fsg;

		if (fsg != null) {
			// Populate every sub-panel with the new simulation group's data
			_simGroupPanel.setSimulationGroup(fsg);
			_simulationPanel.setSimulationGroup(fsg, false);
			_initialConditionsPanel.setSimulationGroup(fsg);
			_operationsPanel.setSimulationGroup(fsg);
			_meteorologyPanel.setSimulationGroup(fsg);
			_tempTargetsPanel.setSimulationGroup(fsg);
			_bcPanel.setSimulationGroup(fsg);

		} else {
			// Clear all sub-panels by passing null as the simulation group
			_simulationPanel.setSimulationGroup(null, false);
			_initialConditionsPanel.setSimulationGroup(null);
			_operationsPanel.setSimulationGroup(null);
			_meteorologyPanel.setSimulationGroup(null);
			_tempTargetsPanel.setSimulationGroup(null);
			_bcPanel.setSimulationGroup(null);

			// Reset all lower-panel controls to their empty state
			clearPanel();
		}
	}

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
		_tempTargetsPanel.setPlanningSet(set);
		_simulationPanel.setPlanningSet(set);
	}

	/**
	 * Reloads the simulation group combo box in the {@link SimulationGroupPanel} to
	 * reflect any changes in the available simulation groups.
	 */
	public void loadSimulationGroupCombo() {
		_simGroupPanel.loadSimulationGroupCombo();
	}

	/**
	 * Programmatically selects the tab that hosts the given {@link AbstractPlanningPanel},
	 * switching the visible tab to that panel.
	 *
	 * Does nothing if {@code panel} is {@code null}.
	 *
	 * @param panel the {@link AbstractPlanningPanel} tab to select; must be one of the
	 *              panels registered as a tab in the tabbed pane
	 */
	public void setSelectedTab(AbstractPlanningPanel panel) {
		if (panel != null) {
			// Switch the tabbed pane's selection to the specified panel component
			_tabbedPane.setSelectedComponent(panel);
		}
	}

	/**
	 * Returns the list of {@link WatSimulation} instances currently selected in the
	 * Simulation tab's simulation table.
	 *
	 * @return a {@link List} of selected {@link WatSimulation} objects; may be empty
	 * but never {@code null}
	 */
	public List<WatSimulation> getSelectedSimulations() {
		return _simulationPanel.getSelectedSimulations();
	}

	/**
	 * Returns the list of {@link ResultsData} items currently selected in the
	 * Simulation tab's results table.
	 *
	 * @return a {@link List} of selected {@link ResultsData} objects; may be empty
	 * but never {@code null}
	 */
	public List<ResultsData> getSelectedResults() {
		return _simulationPanel.getSelectedResults();
	}

	/**
	 * Returns the {@link SimulationPanel} that is hosted in the Simulation tab.
	 *
	 * @return the {@link SimulationPanel}; never {@code null} after construction
	 */
	public SimulationPanel getSimulationPanel() {
		return _simulationPanel;
	}

	/**
	 * Notifies the {@link SimulationGroupPanel} that the simulation group represented
	 * by the given {@link ManagerProxy} has been deleted, so it can update the combo
	 * box accordingly.
	 *
	 * @param proxy the {@link ManagerProxy} representing the deleted simulation group
	 */
	public void simulationGroupDeleted(ManagerProxy proxy) {
		_simGroupPanel.simulationGroupDeleted(proxy);
	}

	/**
	 * Returns the single {@link WatSimulation} currently highlighted (selected) in the
	 * Simulation tab's simulation table.
	 *
	 * @return the highlighted {@link WatSimulation}, or {@code null} if none is selected
	 */
	public WatSimulation getSelectedSimulation() {
		return _simulationPanel.getSelectedSimulation();
	}

	/**
	 * Refreshes the Simulation tab's table, ensemble set list, and analysis window
	 * after a change to the given {@link PlanningSimGroup}'s ensemble sets, then
	 * re-enables the panel and restores the simulation table selection.
	 *
	 * Called after a boundary condition set deletion or other operation that causes
	 * ensemble sets to be added or removed as a side effect.
	 *
	 * @param fsg the {@link PlanningSimGroup} whose updated ensemble sets should be
	 *            reflected in the Simulation tab
	 */
	public void refreshSimulationPanel(PlanningSimGroup fsg) {
		// Capture the currently highlighted simulation before the table is refreshed
		WatSimulation simulation = getSelectedSimulation();

		// Reload the simulation table rows from the updated simulation group data
		_simulationPanel.fillSimulationTable();

		// Update the ensemble set list for the currently selected simulation
		_simulationPanel.setEnsembleSets(fsg.getEnsembleSets(simulation));

		// Refresh the analysis window to reflect the updated ensemble set state
		_simulationPanel.fillAnalysisWindow();

		// Re-enable the panel now that the refresh is complete
		setEnabled(true);

		// Restore the previously highlighted row in the simulation table
		_simulationPanel.refreshSimTableSelection();
	}

	/**
	 * Extends the superclass visibility handling to clear all panels and notify the
	 * simulation panel of a closing event when this panel becomes visible with no
	 * simulation group loaded.
	 *
	 * This guards against the panel being shown in a stale state after a project close
	 * or simulation group removal.
	 *
	 * @param visible {@code true} to show the panel; {@code false} to hide it
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible && _simGroup == null) {
			// No simulation group is loaded; reset all panels to their empty state
			clearPanel();

			// Notify the simulation panel that it is closing so it can clean up
			_simulationPanel.closing();
		}

		super.setVisible(visible);
	}

}
