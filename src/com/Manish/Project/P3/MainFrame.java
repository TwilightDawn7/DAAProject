package com.Manish.Project.P3;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Main application window for the Transportation Network Analyzer.
 *
 * Layout:
 *  ┌─────────────────────────────────────────────┬──────────────┐
 *  │  TITLE BAR                                   │              │
 *  ├─────────────────────────────────────────────┤  CONTROL     │
 *  │                                             │  PANEL       │
 *  │         GRAPH CANVAS (GraphPanel)           │  (JTabbedPane│
 *  │                                             │   Graph /    │
 *  ├─────────────────────────────────────────────┤   Algorithms)│
 *  │  RESULT PANEL (scrollable text)             │              │
 *  └─────────────────────────────────────────────┴──────────────┘
 */
public class MainFrame extends JFrame {

    // ─── Core ─────────────────────────────────────────────────────────────────
    private TransportGraph graph;
    private GraphPanel     graphPanel;
    private ResultPanel    resultPanel;
    private String         sourceCity = null;

    // ─── Graph Tab Controls ───────────────────────────────────────────────────
    private JTextField  cityNameField;
    private JComboBox<String> removeCityCombo;
    private JComboBox<String> roadFromCombo, roadToCombo;
    private JTextField  roadWeightField;
    private JComboBox<String> removeRoadFromCombo, removeRoadToCombo;
    private JButton     addRoadModeBtn;

    // ─── Algorithm Tab Controls ───────────────────────────────────────────────
    private JComboBox<String> sourceCombo;
    private JLabel      sourceCityLabel;
    private JButton     dijkstraBtn, bfsBtn, dfsBtn, mstBtn, reachabilityBtn;
    private JComboBox<String> dijkstraTargetCombo;

    // ─── Colours ──────────────────────────────────────────────────────────────
    private static final Color DARK_BG   = new Color(18, 18, 30);
    private static final Color MID_BG    = new Color(25, 25, 42);
    private static final Color ACCENT    = new Color(80, 130, 230);
    private static final Color FG        = new Color(210, 220, 255);
    private static final Color SUBTLE    = new Color(100, 110, 150);

    // ─── Constructor ──────────────────────────────────────────────────────────

    public MainFrame() {
        graph = new TransportGraph();
        initUI();
        loadSampleData();
        refreshAllCombos();
    }

    // ─── UI Initialisation ────────────────────────────────────────────────────

    private void initUI() {
        setTitle("Transportation Network Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(DARK_BG);

        // Title bar
        add(buildTitleBar(), BorderLayout.NORTH);

        // Graph panel (centre)
        graphPanel  = new GraphPanel(graph, this);
        resultPanel = new ResultPanel();

        JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphPanel, resultPanel);
        vSplit.setResizeWeight(0.72);
        vSplit.setDividerSize(6);
        vSplit.setBackground(DARK_BG);
        vSplit.setBorder(null);

        // Control panel (east)
        JScrollPane ctrlScroll = new JScrollPane(buildControlPanel());
        ctrlScroll.setPreferredSize(new Dimension(290, 0));
        ctrlScroll.setBorder(null);
        ctrlScroll.setBackground(DARK_BG);
        ctrlScroll.getVerticalScrollBar().setUnitIncrement(10);

        add(vSplit,     BorderLayout.CENTER);
        add(ctrlScroll, BorderLayout.EAST);

        setSize(1280, 820);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        // Menu bar
        setJMenuBar(buildMenuBar());
    }

    // ─── Title Bar ────────────────────────────────────────────────────────────

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(10, 10, 20));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(50, 80, 200)));

        JLabel title = new JLabel("  🗺  Transportation Network Analyzer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(new Color(160, 200, 255));
        title.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 0));

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        rightBar.setOpaque(false);

        JButton saveBtn    = tinyButton("💾 Save",  e -> saveGraph());
        JButton loadBtn    = tinyButton("📂 Load",  e -> loadGraph());
        JButton sampleBtn  = tinyButton("🏙 Sample", e -> { graph = new TransportGraph();
            graphPanel.stopAnimation();
            loadSampleData();
            refreshAllCombos();
            graphPanel.repaint();
            resultPanel.setResult("Sample network loaded!"); });
        JButton clearBtn   = tinyButton("🗑 Clear",  e -> { graph.clearHighlights();
            graphPanel.stopAnimation();
            graphPanel.setStatus("");
            graphPanel.repaint();
            resultPanel.clear(); });

        rightBar.add(sampleBtn);
        rightBar.add(saveBtn);
        rightBar.add(loadBtn);
        rightBar.add(clearBtn);

        bar.add(title,    BorderLayout.WEST);
        bar.add(rightBar, BorderLayout.EAST);
        return bar;
    }

    // ─── Menu Bar ─────────────────────────────────────────────────────────────

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(new Color(15, 15, 28));

        JMenu fileMenu = darkMenu("File");
        fileMenu.add(darkItem("Save Graph…",    e -> saveGraph()));
        fileMenu.add(darkItem("Load Graph…",    e -> loadGraph()));
        fileMenu.addSeparator();
        fileMenu.add(darkItem("Export Summary", e -> exportSummary()));
        fileMenu.addSeparator();
        fileMenu.add(darkItem("Exit",           e -> System.exit(0)));

        JMenu helpMenu = darkMenu("Help");
        helpMenu.add(darkItem("How to Use",     e -> showHelp()));
        helpMenu.add(darkItem("About",          e -> showAbout()));

        mb.add(fileMenu);
        mb.add(helpMenu);
        return mb;
    }

    private JMenu darkMenu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(FG);
        m.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return m;
    }

    private JMenuItem darkItem(String text, ActionListener al) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(MID_BG);
        item.setForeground(FG);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.addActionListener(al);
        return item;
    }

    // ─── Control Panel ────────────────────────────────────────────────────────

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(40, 60, 140)));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(DARK_BG);
        tabs.setForeground(FG);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabs.addTab("🏙 Graph",      buildGraphTab());
        tabs.addTab("⚙ Algorithms", buildAlgoTab());

        // Make the tabbed pane fill the whole control panel
        tabs.setPreferredSize(new Dimension(288, 900));
        panel.add(tabs);

        return panel;
    }

    // ── Graph Tab ─────────────────────────────────────────────────────────────

    private JPanel buildGraphTab() {
        JPanel tab = new JPanel();
        tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
        tab.setBackground(DARK_BG);
        tab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ── Add City ──────────────────────────────────────────────────────
        tab.add(sectionLabel("➕  Add City"));

        JPanel addCityRow = hPanel();
        cityNameField = styledField(10);
        cityNameField.addActionListener(e -> addCity(null));
        addCityRow.add(labelFor("Name:"));
        addCityRow.add(cityNameField);
        JButton addCityBtn = actionButton("Add", e -> addCity(null));
        addCityRow.add(addCityBtn);
        tab.add(addCityRow);
        tab.add(hint("Or double-click on canvas"));
        tab.add(vSpace(10));

        // ── Remove City ────────────────────────────────────────────────────
        tab.add(sectionLabel("🗑  Remove City"));
        JPanel remCityRow = hPanel();
        removeCityCombo = styledCombo();
        remCityRow.add(removeCityCombo);
        JButton remCityBtn = dangerButton("Remove", e -> removeSelectedCity());
        remCityRow.add(remCityBtn);
        tab.add(remCityRow);
        tab.add(vSpace(12));

        // ── Add Road ──────────────────────────────────────────────────────
        tab.add(sectionLabel("🛣  Add Road"));

        JPanel fromRow = hPanel();
        fromRow.add(labelFor("From:"));
        roadFromCombo = styledCombo();
        fromRow.add(roadFromCombo);
        tab.add(fromRow);

        JPanel toRow = hPanel();
        toRow.add(labelFor("  To:"));
        roadToCombo = styledCombo();
        toRow.add(roadToCombo);
        tab.add(toRow);

        JPanel wRow = hPanel();
        wRow.add(labelFor(" km:"));
        roadWeightField = styledField(6);
        roadWeightField.setText("100");
        wRow.add(roadWeightField);
        JButton addRoadBtn = actionButton("Add Road", e -> addRoadFromCombo());
        wRow.add(addRoadBtn);
        tab.add(wRow);

        addRoadModeBtn = new JButton("🖱 Click Mode");
        addRoadModeBtn.setToolTipText("Click two cities on canvas to connect them");
        styleButton(addRoadModeBtn, new Color(60, 90, 160));
        addRoadModeBtn.addActionListener(e -> toggleAddRoadMode());
        tab.add(fullWidth(addRoadModeBtn));
        tab.add(vSpace(12));

        // ── Remove Road ────────────────────────────────────────────────────
        tab.add(sectionLabel("✂  Remove Road"));
        JPanel remR1 = hPanel();
        remR1.add(labelFor("From:"));
        removeRoadFromCombo = styledCombo();
        remR1.add(removeRoadFromCombo);
        tab.add(remR1);

        JPanel remR2 = hPanel();
        remR2.add(labelFor("  To:"));
        removeRoadToCombo = styledCombo();
        remR2.add(removeRoadToCombo);
        tab.add(remR2);

        JButton remRoadBtn = dangerButton("Remove Road", e -> removeSelectedRoad());
        tab.add(fullWidth(remRoadBtn));
        tab.add(vSpace(20));

        // ── Graph Info ─────────────────────────────────────────────────────
        tab.add(sectionLabel("📋  Graph Info"));
        JButton infoBtn = actionButton("Show Graph Info", e -> showGraphInfo());
        tab.add(fullWidth(infoBtn));

        return tab;
    }

    // ── Algorithm Tab ─────────────────────────────────────────────────────────

    private JPanel buildAlgoTab() {
        JPanel tab = new JPanel();
        tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
        tab.setBackground(DARK_BG);
        tab.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ── Source Selection ────────────────────────────────────────────────
        tab.add(sectionLabel("🎯  Source City"));
        JPanel srcRow = hPanel();
        sourceCombo = styledCombo();
        JButton setSrcBtn = actionButton("Set", e -> {
            if (sourceCombo.getSelectedItem() != null) {
                setSourceCity((String) sourceCombo.getSelectedItem());
            }
        });
        srcRow.add(sourceCombo);
        srcRow.add(setSrcBtn);
        tab.add(srcRow);

        sourceCityLabel = new JLabel("  No source selected");
        sourceCityLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sourceCityLabel.setForeground(SUBTLE);
        tab.add(sourceCityLabel);
        tab.add(vSpace(14));

        // ── Dijkstra ────────────────────────────────────────────────────────
        tab.add(sectionLabel("🔵  Dijkstra Shortest Paths"));
        tab.add(hint("Finds shortest path from source to ALL cities"));

        JPanel dijRow = hPanel();
        dijRow.add(labelFor("Target:"));
        dijkstraTargetCombo = styledCombo();
        dijkstraTargetCombo.insertItemAt("All Cities", 0);
        dijkstraTargetCombo.setSelectedIndex(0);
        dijRow.add(dijkstraTargetCombo);
        tab.add(dijRow);

        dijkstraBtn = actionButton("▶ Run Dijkstra", e -> runDijkstra());
        tab.add(fullWidth(dijkstraBtn));
        tab.add(vSpace(12));

        // ── BFS ─────────────────────────────────────────────────────────────
        tab.add(sectionLabel("🟢  BFS Traversal"));
        tab.add(hint("Breadth-first exploration with animation"));
        bfsBtn = actionButton("▶ Run BFS", e -> runBFS());
        tab.add(fullWidth(bfsBtn));
        tab.add(vSpace(12));

        // ── DFS ─────────────────────────────────────────────────────────────
        tab.add(sectionLabel("🟣  DFS Traversal"));
        tab.add(hint("Depth-first exploration with animation"));
        dfsBtn = actionButton("▶ Run DFS", e -> runDFS());
        tab.add(fullWidth(dfsBtn));
        tab.add(vSpace(12));

        // ── Reachability ────────────────────────────────────────────────────
        tab.add(sectionLabel("🔍  Reachability Analysis"));
        tab.add(hint("Which cities are reachable from source?"));
        reachabilityBtn = actionButton("▶ Check Reachability", e -> runReachability());
        tab.add(fullWidth(reachabilityBtn));
        tab.add(vSpace(12));

        // ── MST ─────────────────────────────────────────────────────────────
        tab.add(sectionLabel("🌲  Min Spanning Tree (Kruskal's)"));
        tab.add(hint("Minimum cost network connecting all cities"));
        mstBtn = actionButton("▶ Run MST", e -> runMST());
        tab.add(fullWidth(mstBtn));
        tab.add(vSpace(14));

        // ── Clear Highlights ────────────────────────────────────────────────
        JButton clearHighBtn = new JButton("🎨 Clear All Highlights");
        styleButton(clearHighBtn, new Color(60, 60, 90));
        clearHighBtn.addActionListener(e -> {
            graphPanel.stopAnimation();
            graph.clearHighlights();
            graphPanel.setStatus("");
            graphPanel.repaint();
            resultPanel.clear();
        });
        tab.add(fullWidth(clearHighBtn));

        return tab;
    }

    // ─── Algorithm Runners ────────────────────────────────────────────────────

    private void runDijkstra() {
        if (!checkSource()) return;
        graph.clearHighlights();

        DijkstraResult result = DijkstraAlgorithm.compute(graph, sourceCity);

        // Highlight source
        markCity(sourceCity, "source");

        String targetStr = (String) dijkstraTargetCombo.getSelectedItem();
        boolean allMode  = "All Cities".equals(targetStr) || targetStr == null;

        if (allMode) {
            // Highlight all shortest-path edges and nodes
            for (String city : graph.getCityNames()) {
                if (city.equals(sourceCity)) continue;
                List<String> path = result.getPath(city);
                for (String node : path) {
                    if (!node.equals(sourceCity)) markCity(node, "path");
                }
                for (int i = 0; i < path.size() - 1; i++) {
                    markRoad(path.get(i), path.get(i + 1), "path");
                }
            }
            resultPanel.setResult(result.getSummary(graph.getCityNames()));
        } else {
            // Single target
            List<String> path = result.getPath(targetStr);
            if (path.isEmpty()) {
                resultPanel.setResult("=== DIJKSTRA ===\n\n" +
                        targetStr + " is UNREACHABLE from " + sourceCity + ".\n");
            } else {
                for (String node : path) {
                    if (!node.equals(sourceCity)) markCity(node, "path");
                }
                for (int i = 0; i < path.size() - 1; i++) {
                    markRoad(path.get(i), path.get(i + 1), "path");
                }
                resultPanel.setResult("=== DIJKSTRA SHORTEST PATH ===\n\n" +
                        "Source: " + sourceCity + "\n" +
                        "Target: " + targetStr + "\n\n" +
                        "Distance: " + String.format("%.1f", result.getDistance(targetStr)) + " km\n\n" +
                        "Path:\n  " + String.join("  →  ", path) + "\n");
            }
        }
        graphPanel.repaint();
    }

    private void runBFS() {
        if (!checkSource()) return;
        graph.clearHighlights();

        List<String> order = TraversalAlgorithm.bfs(graph, sourceCity);
        resultPanel.setResult(TraversalAlgorithm.formatResult(order, sourceCity, "BFS"));
        graphPanel.startAnimation(order, "bfs");
        setAlgoBtnsEnabled(false);
    }

    private void runDFS() {
        if (!checkSource()) return;
        graph.clearHighlights();

        List<String> order = TraversalAlgorithm.dfs(graph, sourceCity);
        resultPanel.setResult(TraversalAlgorithm.formatResult(order, sourceCity, "DFS"));
        graphPanel.startAnimation(order, "dfs");
        setAlgoBtnsEnabled(false);
    }

    private void runReachability() {
        if (!checkSource()) return;
        graph.clearHighlights();

        DijkstraResult result = DijkstraAlgorithm.compute(graph, sourceCity);
        markCity(sourceCity, "source");

        StringBuilder sb = new StringBuilder();
        sb.append("=== REACHABILITY ANALYSIS ===\n");
        sb.append("Source: ").append(sourceCity).append("\n\n");

        List<String> reachable   = new ArrayList<>();
        List<String> unreachable = new ArrayList<>();

        for (String city : graph.getCityNames()) {
            if (city.equals(sourceCity)) continue;
            if (result.isReachable(city)) {
                reachable.add(city);
                markCity(city, "reachable");
            } else {
                unreachable.add(city);
                markCity(city, "unreachable");
            }
        }

        sb.append("✔ REACHABLE (").append(reachable.size()).append("):\n");
        for (String c : reachable) sb.append("    • ").append(c).append("\n");

        sb.append("\n✘ UNREACHABLE (").append(unreachable.size()).append("):\n");
        if (unreachable.isEmpty()) {
            sb.append("    (none – graph is fully connected from source)\n");
        } else {
            for (String c : unreachable) sb.append("    • ").append(c).append("\n");
        }

        graphPanel.repaint();
        resultPanel.setResult(sb.toString());
    }

    private void runMST() {
        if (graph.getCityCount() < 2) {
            warn("Add at least 2 cities before running MST.");
            return;
        }
        graph.clearHighlights();

        MSTResult mst = MSTAlgorithm.kruskal(graph);

        // Highlight MST edges and their endpoints
        for (Road road : mst.getMstEdges()) {
            road.setHighlightType("mst");
            markCity(road.getFrom(), "mst");
            markCity(road.getTo(),   "mst");
        }

        graphPanel.repaint();
        resultPanel.setResult(mst.getSummary());
    }

    // ─── Graph Callbacks (called from GraphPanel) ─────────────────────────────

    /** Called when a city is clicked in the graph panel. */
    public void onCitySelected(String cityName) {
        // Optionally auto-populate source combo
        sourceCombo.setSelectedItem(cityName);
    }

    /** Called when a traversal animation finishes. */
    public void onAnimationComplete() {
        setAlgoBtnsEnabled(true);
    }

    // ─── Graph Modification (called from GraphPanel context menus) ─────────────

    /** Called by GraphPanel double-click or context menu. */
    public void promptAddCity(int x, int y) {
        String name = JOptionPane.showInputDialog(this,
                "Enter city name:", "Add City", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.isBlank()) return;
        name = name.trim();
        addCity(name, x, y);
    }

    /** Called by GraphPanel road-add mode. */
    public void promptAddRoad(String from, String to) {
        String ws = JOptionPane.showInputDialog(this,
                "Distance from " + from + " to " + to + " (km):",
                "Add Road", JOptionPane.PLAIN_MESSAGE);
        if (ws == null || ws.isBlank()) return;
        try {
            double w = Double.parseDouble(ws.trim());
            if (w <= 0) throw new NumberFormatException();
            if (graph.addRoad(from, to, w)) {
                refreshAllCombos();
                graphPanel.repaint();
                resultPanel.setResult("Road added: " + from + " ↔ " + to +
                        " (" + w + " km)\n");
            } else {
                warn("Road already exists between " + from + " and " + to + ".");
            }
        } catch (NumberFormatException ex) {
            warn("Invalid distance. Enter a positive number.");
        }
    }

    /** Sets the source city, highlights it, refreshes labels. */
    public void setSourceCity(String name) {
        if (!graph.hasCity(name)) return;
        graph.clearHighlights();
        sourceCity = name;
        markCity(name, "source");
        sourceCombo.setSelectedItem(name);
        sourceCityLabel.setText("  Source: " + name);
        sourceCityLabel.setForeground(new Color(80, 220, 120));
        graphPanel.repaint();
    }

    /** Called from GraphPanel context menu. */
    public void removeCity(String name) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove city '" + name + "' and all its roads?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        graph.removeCity(name);
        if (name.equals(sourceCity)) { sourceCity = null; sourceCityLabel.setText("  No source selected"); }
        refreshAllCombos();
        graphPanel.repaint();
        resultPanel.setResult("City removed: " + name);
    }

    // ─── Graph Tab Actions ────────────────────────────────────────────────────

    private void addCity(String overrideName) {
        String name = (overrideName != null) ? overrideName : cityNameField.getText().trim();
        if (name.isEmpty()) { warn("Enter a city name."); return; }

        // Place city at random canvas position
        int margin = City.RADIUS + 20;
        int x = margin + (int)(Math.random() * (graphPanel.getWidth()  - 2 * margin));
        int y = margin + (int)(Math.random() * (graphPanel.getHeight() - 2 * margin));
        addCity(name, x, y);
        cityNameField.setText("");
    }

    private void addCity(String name, int x, int y) {
        if (name == null || name.isBlank()) return;
        name = name.trim();
        if (graph.addCity(new City(name, x, y))) {
            refreshAllCombos();
            graphPanel.repaint();
            resultPanel.setResult("City added: " + name + " at (" + x + ", " + y + ")");
        } else {
            warn("A city named '" + name + "' already exists.");
        }
    }

    private void removeSelectedCity() {
        String name = (String) removeCityCombo.getSelectedItem();
        if (name == null) { warn("No city selected."); return; }
        removeCity(name);
    }

    private void addRoadFromCombo() {
        String from = (String) roadFromCombo.getSelectedItem();
        String to   = (String) roadToCombo.getSelectedItem();
        if (from == null || to == null) { warn("Select both endpoints."); return; }
        if (from.equals(to))            { warn("A road must connect two different cities."); return; }
        try {
            double w = Double.parseDouble(roadWeightField.getText().trim());
            if (w <= 0) throw new NumberFormatException();
            if (graph.addRoad(from, to, w)) {
                refreshAllCombos();
                graphPanel.repaint();
                resultPanel.setResult("Road added: " + from + " ↔ " + to + " (" + w + " km)");
            } else {
                warn("Road between " + from + " and " + to + " already exists.");
            }
        } catch (NumberFormatException ex) {
            warn("Enter a valid positive number for the distance.");
        }
    }

    private void removeSelectedRoad() {
        String from = (String) removeRoadFromCombo.getSelectedItem();
        String to   = (String) removeRoadToCombo.getSelectedItem();
        if (from == null || to == null) { warn("Select both endpoints."); return; }
        if (graph.removeRoad(from, to)) {
            refreshAllCombos();
            graphPanel.repaint();
            resultPanel.setResult("Road removed: " + from + " ↔ " + to);
        } else {
            warn("No road found between " + from + " and " + to + ".");
        }
    }

    private void toggleAddRoadMode() {
        boolean newMode = !graphPanel.isAddRoadMode();
        graphPanel.setAddRoadMode(newMode);
        addRoadModeBtn.setText(newMode ? "✅ Click Mode ON" : "🖱 Click Mode");
        styleButton(addRoadModeBtn, newMode ? new Color(30, 130, 80) : new Color(60, 90, 160));
    }

    private void showGraphInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== GRAPH INFORMATION ===\n\n");
        sb.append("Cities  : ").append(graph.getCityCount()).append("\n");
        sb.append("Roads   : ").append(graph.getAllRoads().size()).append("\n\n");

        sb.append("City List:\n");
        for (City c : graph.getCities()) {
            sb.append(String.format("  %-18s  (%d, %d)\n", c.getName(), c.getX(), c.getY()));
        }
        sb.append("\nRoad List:\n");
        for (Road r : graph.getAllRoads()) {
            sb.append("  ").append(r).append("\n");
        }
        resultPanel.setResult(sb.toString());
    }

    // ─── Save / Load ──────────────────────────────────────────────────────────

    private void saveGraph() {
        JFileChooser fc = styledChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Transport Graph (*.tng)", "tng"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().endsWith(".tng")) file = new File(file.getPath() + ".tng");
            try {
                GraphSerializer.save(graph, file);
                resultPanel.setResult("Graph saved to: " + file.getAbsolutePath());
            } catch (IOException ex) {
                warn("Save failed: " + ex.getMessage());
            }
        }
    }

    private void loadGraph() {
        JFileChooser fc = styledChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Transport Graph (*.tng)", "tng"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                graph = GraphSerializer.load(fc.getSelectedFile());
                graphPanel.stopAnimation();
                rebindGraph();
                refreshAllCombos();
                graphPanel.repaint();
                resultPanel.setResult("Graph loaded from: " + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                warn("Load failed: " + ex.getMessage());
            }
        }
    }

    private void exportSummary() {
        JFileChooser fc = styledChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().endsWith(".txt")) file = new File(file.getPath() + ".txt");
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.print(GraphSerializer.toTextSummary(graph));
                resultPanel.setResult("Summary exported to: " + file.getAbsolutePath());
            } catch (IOException ex) {
                warn("Export failed: " + ex.getMessage());
            }
        }
    }

    // ─── Sample Data ──────────────────────────────────────────────────────────

    private void loadSampleData() {
        // 8 Indian cities as sample nodes (transportation study)
        int[][] pos = {
                {160, 130}, {380,  80}, {620,  90}, {800, 200},
                {650, 320}, {400, 350}, {200, 320}, {480, 500}
        };
        String[] names = {
                "Delhi", "Jaipur", "Agra", "Lucknow",
                "Kanpur", "Gwalior", "Jodhpur", "Bhopal"
        };
        for (int i = 0; i < names.length; i++) {
            graph.addCity(new City(names[i], pos[i][0], pos[i][1]));
        }

        // Roads with approximate distances (km)
        double[][] roads = {
                // from-idx, to-idx, weight
        };
        graph.addRoad("Delhi",    "Jaipur",   281);
        graph.addRoad("Delhi",    "Agra",     233);
        graph.addRoad("Delhi",    "Jodhpur",  600);
        graph.addRoad("Jaipur",   "Agra",     240);
        graph.addRoad("Jaipur",   "Jodhpur",  337);
        graph.addRoad("Jaipur",   "Gwalior",  321);
        graph.addRoad("Agra",     "Lucknow",  363);
        graph.addRoad("Agra",     "Gwalior",  121);
        graph.addRoad("Lucknow",  "Kanpur",    80);
        graph.addRoad("Lucknow",  "Bhopal",   490);
        graph.addRoad("Kanpur",   "Gwalior",  320);
        graph.addRoad("Kanpur",   "Bhopal",   375);
        graph.addRoad("Gwalior",  "Bhopal",   413);
        graph.addRoad("Jodhpur",  "Bhopal",   580);

        setSourceCity("Delhi");
        refreshAllCombos();
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    private void rebindGraph() {
        // Swap graph reference in GraphPanel via reflection-free trick:
        // GraphPanel holds a final ref; since we reconstruct on load, rebuild the panel.
        graphPanel.stopAnimation();
        // Re-create graphPanel with the new graph
        Container contentPane = getContentPane();
        JSplitPane vSplit = (JSplitPane) ((JSplitPane) contentPane.getComponent(1)
                instanceof JSplitPane
                ? contentPane.getComponent(1) : null);
        // Simpler: just rebuild the whole UI
        contentPane.removeAll();
        initUI();
        // Re-assign graph
        loadedGraphHook();
        contentPane.revalidate();
        contentPane.repaint();
    }

    private void loadedGraphHook() {
        // Graph was swapped; make sure graphPanel points to new graph.
        // Because we rebuild in rebindGraph this is already handled by initUI().
    }

    private boolean checkSource() {
        if (sourceCity == null || !graph.hasCity(sourceCity)) {
            warn("Please select a source city first (Algorithms tab → Set).");
            return false;
        }
        if (graph.getCityCount() < 2) {
            warn("Add at least 2 cities to run algorithms.");
            return false;
        }
        return true;
    }

    private void markCity(String name, String type) {
        City c = graph.getCity(name);
        if (c != null) c.setHighlightType(type);
    }

    private void markRoad(String from, String to, String type) {
        for (Road r : graph.getAllRoads()) {
            if (r.connects(from, to)) { r.setHighlightType(type); return; }
        }
    }

    private void refreshAllCombos() {
        String[] names = graph.getCityNames().toArray(new String[0]);

        refreshCombo(removeCityCombo,     names);
        refreshCombo(roadFromCombo,       names);
        refreshCombo(roadToCombo,         names);
        refreshCombo(removeRoadFromCombo, names);
        refreshCombo(removeRoadToCombo,   names);
        refreshCombo(sourceCombo,         names);

        // Dijkstra target includes "All Cities" sentinel
        dijkstraTargetCombo.removeAllItems();
        dijkstraTargetCombo.addItem("All Cities");
        for (String n : names) dijkstraTargetCombo.addItem(n);

        // Restore source selection
        if (sourceCity != null) sourceCombo.setSelectedItem(sourceCity);
    }

    private void refreshCombo(JComboBox<String> combo, String[] names) {
        Object selected = combo.getSelectedItem();
        combo.removeAllItems();
        for (String n : names) combo.addItem(n);
        if (selected != null) combo.setSelectedItem(selected);
    }

    private void setAlgoBtnsEnabled(boolean enabled) {
        dijkstraBtn.setEnabled(enabled);
        bfsBtn.setEnabled(enabled);
        dfsBtn.setEnabled(enabled);
        mstBtn.setEnabled(enabled);
        reachabilityBtn.setEnabled(enabled);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private JFileChooser styledChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        return fc;
    }

    private void showHelp() {
        String help =
                "TRANSPORTATION NETWORK ANALYZER – HOW TO USE\n\n" +
                        "ADDING CITIES:\n" +
                        "  • Type a name in the Graph tab and press Add.\n" +
                        "  • Or double-click on the canvas.\n" +
                        "  • Or right-click on empty canvas.\n\n" +
                        "ADDING ROADS:\n" +
                        "  • Select from/to cities and distance, click Add Road.\n" +
                        "  • Or click 'Click Mode' then click two cities on canvas.\n\n" +
                        "RUNNING ALGORITHMS:\n" +
                        "  1. Go to Algorithms tab.\n" +
                        "  2. Select a source city and click Set.\n" +
                        "  3. Choose an algorithm and click Run.\n\n" +
                        "COLOUR LEGEND:\n" +
                        "  🔵 Blue   = Normal city\n" +
                        "  🟢 Green  = Source city\n" +
                        "  🟠 Orange = Shortest path\n" +
                        "  🩵 Cyan   = BFS visited\n" +
                        "  🟣 Purple = DFS visited\n" +
                        "  🟡 Gold   = MST city\n" +
                        "  🟩 Lime   = Reachable\n" +
                        "  🔴 Red    = Unreachable\n";
        JOptionPane.showMessageDialog(this, help, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Transportation Network Analyzer\n" +
                        "Version 1.0\n\n" +
                        "Algorithms implemented:\n" +
                        "  • Dijkstra's Shortest Path\n" +
                        "  • BFS / DFS Traversal\n" +
                        "  • Kruskal's Minimum Spanning Tree\n\n" +
                        "Data Structures: Adjacency List, PriorityQueue, Union-Find\n",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── UI Helper Factory Methods ────────────────────────────────────────────

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(140, 175, 255));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        return lbl;
    }

    private JLabel hint(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl.setForeground(SUBTLE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return lbl;
    }

    private JLabel labelFor(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(FG);
        return lbl;
    }

    private JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(new Color(28, 28, 48));
        tf.setForeground(FG);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 150)),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        return tf;
    }

    private JComboBox<String> styledCombo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(new Color(28, 28, 48));
        cb.setForeground(FG);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setPreferredSize(new Dimension(130, 26));
        return cb;
    }

    private JButton actionButton(String text, ActionListener al) {
        JButton btn = new JButton(text);
        styleButton(btn, ACCENT);
        btn.addActionListener(al);
        return btn;
    }

    private JButton dangerButton(String text, ActionListener al) {
        JButton btn = new JButton(text);
        styleButton(btn, new Color(160, 50, 50));
        btn.addActionListener(al);
        return btn;
    }

    private JButton tinyButton(String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(new Color(35, 55, 110));
        btn.setForeground(FG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 90, 180)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.addActionListener(al);
        return btn;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.brighter()),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JPanel hPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setBackground(DARK_BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private Component vSpace(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    private JPanel fullWidth(JButton btn) {
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(DARK_BG);
        wrap.add(btn, BorderLayout.CENTER);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrap;
    }
}
