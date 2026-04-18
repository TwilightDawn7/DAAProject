package com.Manish.Project.P3;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * South panel that displays algorithm results, instructions, and status messages
 * in a scrollable, read-only text area with a dark monospace theme.
 */
public class ResultPanel extends JPanel {

    private final JTextArea textArea;

    public ResultPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(18, 18, 30));
        setPreferredSize(new Dimension(0, 200));

        // ── Styled border ──────────────────────────────────────────────────
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(50, 80, 160)),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(2, 8, 4, 8),
                        " 📊 Analysis Output ",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        new Color(140, 175, 255)
                )
        ));

        // ── Text area ─────────────────────────────────────────────────────
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(new Color(12, 12, 22));
        textArea.setForeground(new Color(200, 220, 255));
        textArea.setCaretColor(new Color(100, 180, 255));
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(6, 10, 6, 10));
        textArea.setText(getWelcomeText());

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(null);
        scroll.setBackground(new Color(12, 12, 22));
        scroll.getVerticalScrollBar().setBackground(new Color(25, 25, 45));

        add(scroll, BorderLayout.CENTER);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Replace the current output text. */
    public void setResult(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    /** Append text without clearing existing content. */
    public void appendResult(String text) {
        textArea.append(text);
    }

    /** Clear all text. */
    public void clear() {
        textArea.setText("");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static String getWelcomeText() {
        return
                "╔══════════════════════════════════════════════════════════════╗\n" +
                        "║       Transportation Network Analyzer  –  Ready              ║\n" +
                        "╚══════════════════════════════════════════════════════════════╝\n\n" +
                        "  HOW TO USE:\n" +
                        "  • A sample network of 8 cities has been loaded for you.\n" +
                        "  • Double-click on the canvas to add a new city.\n" +
                        "  • Right-click on a city for context menu options.\n" +
                        "  • Drag cities to reposition them on the canvas.\n\n" +
                        "  GRAPH MANAGEMENT (right panel):\n" +
                        "  • Use the Graph tab to add / remove cities and roads.\n" +
                        "  • Click 'Add Road (Click Mode)' then click two cities.\n\n" +
                        "  ALGORITHMS (right panel → Algorithms tab):\n" +
                        "  • Select a source city, then run Dijkstra / BFS / DFS / MST.\n\n" +
                        "  Results will appear here after each algorithm run.\n";
    }
}
