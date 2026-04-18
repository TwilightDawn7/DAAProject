package com.Manish.Project.P3;

import javax.swing.*;

/**
 * Entry point for the Transportation Network Analyzer application.
 *
 * Launches the Swing GUI on the Event Dispatch Thread (EDT) as required
 * by Swing's single-threaded model.
 *
 * To run:
 *   javac *.java
 *   java TransportationApp
 */
public class TransportationApp {

    public static void main(String[] args) {
        // Apply system look-and-feel for native window decorations
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // Dark scroll bars
            UIManager.put("ScrollBar.background",   new java.awt.Color(25, 25, 45));
            UIManager.put("ScrollBar.thumb",         new java.awt.Color(70, 90, 160));
            UIManager.put("ScrollBar.thumbDarkShadow", new java.awt.Color(40, 40, 80));
            UIManager.put("ScrollBar.thumbHighlight", new java.awt.Color(100, 120, 200));
            UIManager.put("ScrollBar.track",         new java.awt.Color(18, 18, 30));

            // Dark popup menus
            UIManager.put("PopupMenu.background",    new java.awt.Color(25, 25, 42));
            UIManager.put("PopupMenu.foreground",    new java.awt.Color(210, 220, 255));
            UIManager.put("MenuItem.background",     new java.awt.Color(25, 25, 42));
            UIManager.put("MenuItem.foreground",     new java.awt.Color(210, 220, 255));
            UIManager.put("MenuItem.selectionBackground", new java.awt.Color(60, 90, 180));
            UIManager.put("MenuItem.selectionForeground", java.awt.Color.WHITE);
            UIManager.put("Menu.background",         new java.awt.Color(15, 15, 28));
            UIManager.put("Menu.foreground",         new java.awt.Color(210, 220, 255));
            UIManager.put("MenuBar.background",      new java.awt.Color(15, 15, 28));
            UIManager.put("MenuBar.foreground",      new java.awt.Color(210, 220, 255));
            UIManager.put("TabbedPane.background",   new java.awt.Color(18, 18, 30));
            UIManager.put("TabbedPane.foreground",   new java.awt.Color(200, 215, 255));
            UIManager.put("TabbedPane.selected",     new java.awt.Color(40, 65, 140));
            UIManager.put("ComboBox.background",     new java.awt.Color(28, 28, 48));
            UIManager.put("ComboBox.foreground",     new java.awt.Color(210, 220, 255));
            UIManager.put("OptionPane.background",   new java.awt.Color(18, 18, 30));
            UIManager.put("OptionPane.messageForeground", new java.awt.Color(210, 220, 255));
            UIManager.put("Panel.background",        new java.awt.Color(18, 18, 30));
            UIManager.put("Label.foreground",        new java.awt.Color(210, 220, 255));
        } catch (Exception ignored) {
            // Fall back to default L&F
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
