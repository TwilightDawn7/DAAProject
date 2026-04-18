package com.Manish.Project.P3;

import java.util.*;

/**
 * Immutable result object returned by {@link DijkstraAlgorithm}.
 * Stores shortest distances and predecessor maps for path reconstruction.
 */
public class DijkstraResult {

    private final String              source;
    private final Map<String, Double> distances;   // city → shortest distance from source
    private final Map<String, String> previous;    // city → predecessor city in shortest path

    public DijkstraResult(String source,
                          Map<String, Double> distances,
                          Map<String, String> previous) {
        this.source    = source;
        this.distances = Collections.unmodifiableMap(distances);
        this.previous  = Collections.unmodifiableMap(previous);
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public String getSource() { return source; }

    /** Returns the shortest distance to {@code city}, or ∞ if unreachable. */
    public double getDistance(String city) {
        return distances.getOrDefault(city, Double.POSITIVE_INFINITY);
    }

    /** Returns true if {@code city} is reachable from the source. */
    public boolean isReachable(String city) {
        Double d = distances.get(city);
        return d != null && d < Double.POSITIVE_INFINITY;
    }

    /** All distances map (city → distance). */
    public Map<String, Double> getAllDistances() { return distances; }

    // ─── Path Reconstruction ──────────────────────────────────────────────────

    /**
     * Reconstructs the shortest path from source to {@code target}
     * by following predecessor links.
     *
     * @return Ordered list of city names [source, ..., target],
     *         or an empty list if target is unreachable.
     */
    public List<String> getPath(String target) {
        if (!isReachable(target) && !target.equals(source)) return Collections.emptyList();

        LinkedList<String> path = new LinkedList<>();
        String current = target;
        int guard = 0;                          // prevent infinite loop on broken data

        while (current != null && guard++ < 1000) {
            path.addFirst(current);
            if (current.equals(source)) break;
            current = previous.get(current);
        }

        // Validate that we reached the source
        if (path.isEmpty() || !path.getFirst().equals(source)) return Collections.emptyList();
        return path;
    }

    /**
     * Returns a formatted multi-line summary of all shortest paths.
     */
    public String getSummary(Collection<String> allCities) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DIJKSTRA'S SHORTEST PATHS ===\n");
        sb.append("Source: ").append(source).append("\n\n");

        for (String city : allCities) {
            if (city.equals(source)) continue;
            if (isReachable(city)) {
                List<String> path = getPath(city);
                sb.append(String.format("%-18s  dist = %7.1f km\n", city, getDistance(city)));
                sb.append("  Path: ").append(String.join(" → ", path)).append("\n");
            } else {
                sb.append(String.format("%-18s  UNREACHABLE\n", city));
            }
        }
        return sb.toString();
    }
}