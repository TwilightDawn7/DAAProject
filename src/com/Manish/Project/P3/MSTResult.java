package com.Manish.Project.P3;

import java.util.*;

/**
 * Immutable result object returned by {@link MSTAlgorithm}.
 * Holds the list of edges in the Minimum Spanning Tree and the total cost.
 */
public class MSTResult {

    private final List<Road> mstEdges;
    private final double     totalCost;
    private final boolean    isConnected;   // false when graph is disconnected

    public MSTResult(List<Road> mstEdges, double totalCost, boolean isConnected) {
        this.mstEdges    = Collections.unmodifiableList(mstEdges);
        this.totalCost   = totalCost;
        this.isConnected = isConnected;
    }

    public List<Road> getMstEdges()  { return mstEdges; }
    public double     getTotalCost() { return totalCost; }
    public boolean    isConnected()  { return isConnected; }

    /**
     * Returns a formatted summary string for display in the result panel.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MINIMUM SPANNING TREE (Kruskal's) ===\n\n");

        if (!isConnected) {
            sb.append("⚠ Graph is NOT fully connected.\n");
            sb.append("  MST covers the largest connected component.\n\n");
        }

        sb.append("MST Edges (sorted by weight):\n");
        List<Road> sorted = new ArrayList<>(mstEdges);
        sorted.sort(Comparator.comparingDouble(Road::getWeight));

        for (int i = 0; i < sorted.size(); i++) {
            Road r = sorted.get(i);
            sb.append(String.format("  %2d. %-14s <-> %-14s  %.1f km\n",
                    i + 1, r.getFrom(), r.getTo(), r.getWeight()));
        }

        sb.append(String.format("\nTotal MST cost : %.1f km\n", totalCost));
        sb.append("Edges in MST   : ").append(mstEdges.size()).append("\n");
        return sb.toString();
    }
}
