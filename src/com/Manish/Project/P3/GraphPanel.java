package com.Manish.Project.P3;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Interactive canvas for rendering and interacting with the transportation network.
 *
 * Features:
 *  • Renders cities (nodes) and roads (edges) with colour-coded highlights.
 *  • Drag-and-drop city repositioning.
 *  • Double-click on empty space → add city.
 *  • Right-click context menu on city or canvas.
 *  • "Road Add Mode": click two cities in succession to add a road.
 *  • Step-by-step animation of traversal algorithms via a Swing Timer.
 */
public class GraphPanel extends JPanel {

    // ─── Colours ──────────────────────────────────────────────────────────────
    private static final Color BG_COLOR            = new Color(12, 12, 22);
    private static final Color GRID_COLOR          = new Color(25, 25, 45);
    private static final Color EDGE_NORMAL         = new Color(90,  90, 130);
    private static final Color EDGE_PATH           = new Color(255, 140,   0);
    private static final Color EDGE_MST            = new Color( 50, 230,  80);
    private static final Color STATUS_COLOR        = new Color(255, 230,  80);
    private static final Color PANEL_HINT_BG       = new Color(  0,   0,   0, 160);

    // ─── Dependencies ─────────────────────────────────────────────────────────
    private final TransportGraph graph;
    private final MainFrame      mainFrame;

    // ─── Interaction State ────────────────────────────────────────────────────
    private City    draggingCity   = null;
    private int     dragOffsetX, dragOffsetY;
    private boolean addRoadMode    = false;
    private City    firstRoadCity  = null;   // first click in road-add mode

    // ─── Animation State ──────────────────────────────────────────────────────
    private List<String>    animSequence    = new ArrayList<>();
    private int             animStep        = 0;
    private javax.swing.Timer animTimer     = null;
    private String          animType        = "";     // "bfs" or "dfs"
    private String          statusMessage   = "";

    // ─── Constructor ──────────────────────────────────────────────────────────

    public GraphPanel(TransportGraph graph, MainFrame mainFrame) {
        this.graph     = graph;
        this.mainFrame = mainFrame;

        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(900, 600));
        setDoubleBuffered(true);

        setupMouseListeners();
    }

    // ─── Mouse Interaction ────────────────────────────────────────────────────

    private void setupMouseListeners() {

        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.getX(), e.getY(), e);
                    return;
                }

                City hit = cityAt(e.getX(), e.getY());

                // ── Road-Add Mode ──────────────────────────────────────────
                if (addRoadMode) {
                    if (hit == null) return;
                    if (firstRoadCity == null) {
                        firstRoadCity = hit;
                        hit.setHighlightType("selected");
                        setStatus("Road Add: now click the destination city.");
                        repaint();
                    } else if (!hit.equals(firstRoadCity)) {
                        // Trigger the weight dialog in MainFrame
                        String from = firstRoadCity.getName();
                        firstRoadCity.setHighlightType("normal");
                        firstRoadCity = null;
                        setAddRoadMode(false);
                        mainFrame.promptAddRoad(from, hit.getName());
                        repaint();
                    }
                    return;
                }

                // ── Normal Mode ────────────────────────────────────────────
                if (hit != null) {
                    clearSelections();
                    hit.setHighlightType("selected");
                    draggingCity = hit;
                    dragOffsetX  = e.getX() - hit.getX();
                    dragOffsetY  = e.getY() - hit.getY();
                    mainFrame.onCitySelected(hit.getName());
                } else {
                    clearSelections();
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingCity = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double-click on empty canvas → add city
                if (e.getClickCount() == 2
                        && SwingUtilities.isLeftMouseButton(e)
                        && !addRoadMode
                        && cityAt(e.getX(), e.getY()) == null) {
                    mainFrame.promptAddCity(e.getX(), e.getY());
                }
            }
        };

        MouseMotionAdapter mma = new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingCity != null) {
                    int nx = clamp(e.getX() - dragOffsetX, City.RADIUS, getWidth()  - City.RADIUS);
                    int ny = clamp(e.getY() - dragOffsetY, City.RADIUS, getHeight() - City.RADIUS);
                    draggingCity.setX(nx);
                    draggingCity.setY(ny);
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                City hit = cityAt(e.getX(), e.getY());
                setCursor(hit != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : (addRoadMode
                        ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                        : Cursor.getDefaultCursor()));
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(mma);
    }

    private void showContextMenu(int x, int y, MouseEvent e) {
        City hit = cityAt(x, y);
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(30, 30, 50));

        if (hit != null) {
            JMenuItem setSource = styledMenuItem("🎯  Set as Source: " + hit.getName());
            setSource.addActionListener(ev -> mainFrame.setSourceCity(hit.getName()));

            JMenuItem remove = styledMenuItem("🗑  Remove City: " + hit.getName());
            remove.addActionListener(ev -> mainFrame.removeCity(hit.getName()));

            menu.add(setSource);
            menu.addSeparator();
            menu.add(remove);
        } else {
            JMenuItem addCity = styledMenuItem("➕  Add City Here");
            addCity.addActionListener(ev -> mainFrame.promptAddCity(x, y));
            menu.add(addCity);
        }
        menu.show(this, x, y);
    }

    private JMenuItem styledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(new Color(30, 30, 50));
        item.setForeground(new Color(200, 210, 255));
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return item;
    }

    // ─── Road-Add Mode ────────────────────────────────────────────────────────

    public void setAddRoadMode(boolean enabled) {
        this.addRoadMode   = enabled;
        this.firstRoadCity = null;
        if (!enabled) {
            clearSelections();
            setStatus("");
        } else {
            setStatus("Road Add Mode: click the SOURCE city.");
        }
        setCursor(enabled
                ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                : Cursor.getDefaultCursor());
        repaint();
    }

    public boolean isAddRoadMode() { return addRoadMode; }

    // ─── Traversal Animation ──────────────────────────────────────────────────

    /**
     * Starts an animated step-through of {@code sequence}.
     *
     * @param sequence Ordered city names to highlight.
     * @param type     "bfs" or "dfs" (maps to City highlight type).
     */
    public void startAnimation(List<String> sequence, String type) {
        stopAnimation();
        graph.clearHighlights();

        this.animSequence = new ArrayList<>(sequence);
        this.animStep     = 0;
        this.animType     = type;

        setStatus(type.toUpperCase() + " Traversal – animating …");
        repaint();

        animTimer = new Timer(650, e -> {
            if (animStep < animSequence.size()) {
                // Mark previous "current" as visited type
                if (animStep > 0) {
                    City prev = graph.getCity(animSequence.get(animStep - 1));
                    if (prev != null) prev.setHighlightType(animType);
                }
                // Highlight current node
                City cur = graph.getCity(animSequence.get(animStep));
                if (cur != null) cur.setHighlightType("current");

                setStatus(type.toUpperCase() + " – visiting: " + animSequence.get(animStep)
                        + "  (step " + (animStep + 1) + "/" + animSequence.size() + ")");
                animStep++;
                repaint();
            } else {
                // Mark last city
                if (!animSequence.isEmpty()) {
                    City last = graph.getCity(animSequence.get(animSequence.size() - 1));
                    if (last != null) last.setHighlightType(animType);
                }
                stopAnimation();
                setStatus(type.toUpperCase() + " complete. " + animSequence.size() + " cities visited.");
                mainFrame.onAnimationComplete();
                repaint();
            }
        });
        animTimer.start();
    }

    public void stopAnimation() {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
    }

    // ─── Status ───────────────────────────────────────────────────────────────

    public void setStatus(String msg) { this.statusMessage = msg; repaint(); }

    // ─── Painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        drawGrid(g2);
        drawRoads(g2);
        drawCities(g2);
        drawStatusBar(g2);
    }

    // ── Grid ──────────────────────────────────────────────────────────────────

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.5f));
        int gs = 60;
        for (int x = 0; x < getWidth();  x += gs) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += gs) g2.drawLine(0, y, getWidth(), y);
    }

    // ── Roads ─────────────────────────────────────────────────────────────────

    private void drawRoads(Graphics2D g2) {
        for (Road road : graph.getAllRoads()) {
            City from = graph.getCity(road.getFrom());
            City to   = graph.getCity(road.getTo());
            if (from == null || to == null) continue;

            Color  col   = edgeColor(road);
            float  width = road.getHighlightType().equals("normal") ? 2f : 4f;

            // Glow behind highlighted edges
            if (!road.getHighlightType().equals("normal")) {
                g2.setStroke(new BasicStroke(width + 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 55));
                g2.drawLine(from.getX(), from.getY(), to.getX(), to.getY());
            }

            g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(col);
            g2.drawLine(from.getX(), from.getY(), to.getX(), to.getY());

            // Weight label
            drawWeightLabel(g2, from, to, road, col);
        }
    }

    private Color edgeColor(Road road) {
        switch (road.getHighlightType()) {
            case "path": return EDGE_PATH;
            case "mst":  return EDGE_MST;
            default:     return EDGE_NORMAL;
        }
    }

    private void drawWeightLabel(Graphics2D g2, City from, City to, Road road, Color col) {
        int mx = (from.getX() + to.getX()) / 2;
        int my = (from.getY() + to.getY()) / 2;

        String text = String.format("%.0f", road.getWeight());
        Font   font = new Font("Consolas", Font.BOLD, 11);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();

        // Background pill
        g2.setColor(new Color(10, 10, 20, 200));
        g2.fillRoundRect(mx - tw/2 - 5, my - th/2 - 1, tw + 10, th + 2, 8, 8);

        // Text
        g2.setColor(col.brighter());
        g2.drawString(text, mx - tw/2, my + fm.getAscent()/2 - 1);
    }

    // ── Cities ────────────────────────────────────────────────────────────────

    private void drawCities(Graphics2D g2) {
        for (City city : graph.getCities()) {
            drawCity(g2, city);
        }
    }

    private void drawCity(Graphics2D g2, City city) {
        int   cx    = city.getX();
        int   cy    = city.getY();
        int   r     = City.RADIUS;
        Color color = city.getColor();

        // Glow rings for highlighted nodes
        if (!"normal".equals(city.getHighlightType())) {
            for (int i = 3; i >= 1; i--) {
                int alpha = 20 * i;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                int gr = r + i * 6;
                g2.fillOval(cx - gr, cy - gr, gr * 2, gr * 2);
            }
        }

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(cx - r + 3, cy - r + 3, r * 2, r * 2);

        // City circle with radial gradient feel (paint manually)
        g2.setColor(color.darker());
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        Color highlight = new Color(
                Math.min(255, color.getRed()   + 60),
                Math.min(255, color.getGreen() + 60),
                Math.min(255, color.getBlue()  + 60));
        g2.setColor(highlight);
        g2.fillOval(cx - r + 4, cy - r + 4, r - 6, r - 6);   // inner highlight

        g2.setColor(color);
        g2.fillOval(cx - r + 2, cy - r + 2, r * 2 - 4, r * 2 - 4);

        // Border ring
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(highlight);
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);

        // City name inside circle
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String name = abbreviate(city.getName(), 6);
        int tx = cx - fm.stringWidth(name) / 2;
        int ty = cy + fm.getAscent() / 2 - 2;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(name, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(name, tx, ty);

        // Full name label below circle
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        fm = g2.getFontMetrics();
        int lw = fm.stringWidth(city.getName());
        g2.setColor(PANEL_HINT_BG);
        g2.fillRoundRect(cx - lw/2 - 4, cy + r + 3, lw + 8, fm.getHeight() + 2, 5, 5);
        g2.setColor(new Color(220, 230, 255));
        g2.drawString(city.getName(), cx - lw/2, cy + r + fm.getAscent() + 3);
    }

    // ── Status Bar ────────────────────────────────────────────────────────────

    private void drawStatusBar(Graphics2D g2) {
        if (statusMessage == null || statusMessage.isEmpty()) return;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int sw = fm.stringWidth(statusMessage);
        int sx = (getWidth() - sw) / 2;
        int sy = getHeight() - 14;

        g2.setColor(PANEL_HINT_BG);
        g2.fillRoundRect(sx - 10, sy - fm.getAscent() - 4, sw + 20, fm.getHeight() + 8, 10, 10);
        g2.setColor(STATUS_COLOR);
        g2.drawString(statusMessage, sx, sy);
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    private City cityAt(int x, int y) {
        // Iterate in reverse so topmost-rendered city is hit first
        List<City> list = new ArrayList<>(graph.getCities());
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).contains(x, y)) return list.get(i);
        }
        return null;
    }

    private void clearSelections() {
        for (City c : graph.getCities()) {
            if ("selected".equals(c.getHighlightType())) c.setHighlightType("normal");
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** Abbreviates a string to at most {@code maxLen} characters. */
    private static String abbreviate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}
