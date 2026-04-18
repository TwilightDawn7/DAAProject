package com.Manish.Project.P3;

import java.io.Serializable;

/**
 * Represents a road (edge) between two cities in the transportation network.
 * The graph is undirected, so a single Road object is shared in both
 * cities' adjacency lists.
 */
public class Road implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String from;
    private final String to;
    private double weight;          // distance / cost in km

    /**
     * Visual highlight type for algorithm visualization.
     * Possible values: normal | path | mst
     */
    private transient String highlightType = "normal";

    public Road(String from, String to, double weight) {
        this.from   = from;
        this.to     = to;
        this.weight = weight;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getFrom()               { return from; }
    public String getTo()                 { return to; }
    public double getWeight()             { return weight; }
    public void   setWeight(double w)     { this.weight = w; }

    public String getHighlightType() {
        return highlightType != null ? highlightType : "normal";
    }
    public void setHighlightType(String type) { this.highlightType = type; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns true if this road connects city1 and city2 (in either direction).
     */
    public boolean connects(String city1, String city2) {
        return (from.equals(city1) && to.equals(city2))
                || (from.equals(city2) && to.equals(city1));
    }

    /**
     * Given one endpoint, return the other.
     */
    public String getOtherEnd(String cityName) {
        return from.equals(cityName) ? to : from;
    }

    /** Called after deserialization to restore transient defaults. */
    private Object readResolve() {
        this.highlightType = "normal";
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s <-> %s  (%.1f km)", from, to, weight);
    }
}