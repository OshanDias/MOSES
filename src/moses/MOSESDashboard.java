package moses;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI for M.O.S.E.S. using Java Swing.
 * Table is now wired to live graph data, and the user can pick which
 * criteria to optimize for and simulate a disruption to see re-routing happen live.
 */
public class MOSESDashboard extends JFrame {
    private SupplyGraph supplyChain;
    private JTextArea resultArea;
    private JTable routeTable;
    private DefaultTableModel tableModel;
    private JComboBox<OptimizationCriteria> criteriaSelector;

    private Location shanghai, colombo, rotterdam, london;

    public MOSESDashboard() {
        setTitle("M.O.S.E.S. - Supply Chain Optimization Dashboard");
        setSize(900, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        supplyChain = new SupplyGraph();
        setupSampleRoutes();

        setupHeader();
        setupCenterTable();
        setupControlPanel();

        refreshTable();

        getContentPane().setBackground(new Color(236, 240, 241));
    }

    private void setupHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(39, 174, 96));
        JLabel title = new JLabel("M.O.S.E.S. GLOBAL ROUTE OPTIMIZER");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Verdana", Font.BOLD, 22));
        header.add(title);
        add(header, BorderLayout.NORTH);
    }

    private void setupCenterTable() {
        String[] columns = {"Origin", "Destination", "Mode", "Cost ($)", "Time (h)", "CO2 (kg)", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        routeTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(routeTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Global Routes"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topRow.add(new JLabel("Optimize by:"));
        criteriaSelector = new JComboBox<>(OptimizationCriteria.values());
        topRow.add(criteriaSelector);

        JButton optimizeBtn = new JButton("FIND BEST ROUTE: SHANGHAI -> LONDON");
        optimizeBtn.setBackground(new Color(44, 62, 80));
        optimizeBtn.setForeground(Color.WHITE);
        optimizeBtn.addActionListener(e -> runOptimization());
        topRow.add(optimizeBtn);

        JButton disruptBtn = new JButton("SIMULATE DISRUPTION (Colombo->Rotterdam SEA)");
        disruptBtn.setBackground(new Color(192, 57, 43));
        disruptBtn.setForeground(Color.WHITE);
        disruptBtn.addActionListener(e -> {
            supplyChain.disruptRoute(colombo, rotterdam, "SEA");
            refreshTable();
            resultArea.setText("Colombo -> Rotterdam (SEA) is now DISRUPTED.\nClick 'Find Best Route' again to see automatic re-routing.\n");
        });
        topRow.add(disruptBtn);

        JButton resetBtn = new JButton("RESET DISRUPTIONS");
        resetBtn.addActionListener(e -> {
            supplyChain.clearAllDisruptions();
            refreshTable();
            resultArea.setText("All disruptions cleared.\n");
        });
        topRow.add(resetBtn);

        resultArea = new JTextArea(10, 20);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Optimization Results (Dijkstra's Algorithm)"));

        controlPanel.add(topRow, BorderLayout.NORTH);
        controlPanel.add(resultScroll, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void setupSampleRoutes() {
        shanghai  = new Location("SHA", "Shanghai Port");
        colombo   = new Location("CMB", "Colombo Port");
        rotterdam = new Location("RTM", "Rotterdam Port");
        london    = new Location("LHR", "London Warehouse");

        supplyChain.addRoute(shanghai, colombo, 2000.0, 240, 1800, "SEA");
        supplyChain.addRoute(shanghai, colombo, 8000.0, 18, 5200, "AIR");
        supplyChain.addRoute(colombo, rotterdam, 3000.0, 288, 2400, "SEA");
        supplyChain.addRoute(rotterdam, london, 500.0, 8, 150, "LAND");
        supplyChain.addRoute(shanghai, london, 15000.0, 22, 9800, "AIR");
    }

    // Reads directly from the graph's real route data - this was the hardcoded part before
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Location origin : supplyChain.getAllLocations()) {
            for (Route r : supplyChain.getRoutesFrom(origin)) {
                tableModel.addRow(new Object[]{
                        origin.getName(),
                        r.getDestination().getName(),
                        r.getTransportMode(),
                        String.format("$%.2f", r.getCost()),
                        r.getTransitTimeHours(),
                        r.getCarbonFootprintKg(),
                        r.isDisrupted() ? "BLOCKED" : "Open"
                });
            }
        }
    }

    private void runOptimization() {
        OptimizationCriteria criteria = (OptimizationCriteria) criteriaSelector.getSelectedItem();
        resultArea.setText("Running Dijkstra's Algorithm (optimizing for " + criteria + ")...\n\n");

        List<Route> path = supplyChain.getPath(shanghai, london, criteria);
        if (path.isEmpty()) {
            resultArea.append("No available route - all paths are disrupted.\n");
            return;
        }

        double totalCost = 0, totalTime = 0, totalCarbon = 0;
        StringBuilder route = new StringBuilder(shanghai.getName());
        for (Route leg : path) {
            route.append(" -[").append(leg.getTransportMode()).append("]-> ").append(leg.getDestination().getName());
            totalCost += leg.getCost();
            totalTime += leg.getTransitTimeHours();
            totalCarbon += leg.getCarbonFootprintKg();
        }
        resultArea.append(route + "\n\n");
        resultArea.append(String.format("Total Cost:  $%.2f%n", totalCost));
        resultArea.append(String.format("Total Time:  %.1f hours%n", totalTime));
        resultArea.append(String.format("Total CO2:   %.0f kg%n", totalCarbon));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MOSESDashboard().setVisible(true));
    }
}