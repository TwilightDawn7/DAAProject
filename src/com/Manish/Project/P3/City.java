package com.Manish.Project.P3;

import java.awt.Color;
import java.io.Serializable;

/**
 * Represents a city (node) in the transportation network graph.
 * Stores position for visualization and highlight state for algorithm display.
 */
public class City implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Radius of the city circle in the visualization */
    public static final int RADIUS = 28;

    private String name;
    private int x, y;

    /**
     * Visual highlight type for algorithm visualization.
     * Possible values: normal | source | path | bfs | dfs |
     *                  reachable | unreachable | mst | selected | current
     */
    private transient String highlightType = "normal";

    public City(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getName()            { return name; }
    public void   setName(String name) { this.name = name; }
    public int    getX()               { return x; }
    public void   setX(int x)          { this.x = x; }
    public int    getY()               { return y; }
    public void   setY(int y)          { this.y = y; }

    public String getHighlightType() {
        return highlightType != null ? highlightType : "normal";
    }
    public void setHighlightType(String type) { this.highlightType = type; }

    // ─── Hit Detection ────────────────────────────────────────────────────────

    /** Returns true if the point (px, py) falls within this city's circle. */
    public boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }

    // ─── Visual Color Mapping ─────────────────────────────────────────────────

    /**
     * Maps highlight type to a display colour so GraphPanel stays clean.
     */
    public Color getColor() {
        switch (getHighlightType()) {
            case "source":      return new Color(46,  213, 115);  // vivid green
            case "path":        return new Color(255, 107,  53);  // orange
            case "bfs":         return new Color(  0, 188, 212);  // cyan
            case "dfs":         return new Color(156,  39, 176);  // purple
            case "current":     return new Color(  0, 229, 255);  // bright cyan
            case "reachable":   return new Color(100, 221,  23);  // lime green
            case "unreachable": return new Color(255,  68,  68);  // red
            case "mst":         return new Color(255, 215,   0);  // gold
            case "selected":    return new Color(255, 165,   0);  // amber
            default:            return new Color( 74, 144, 217);  // steel blue
        }
    }

    /** Called after deserialization to restore transient defaults. */
    private Object readResolve() {
        this.highlightType = "normal";
        return this;
    }

    @Override
    public String toString() { return name; }
}
