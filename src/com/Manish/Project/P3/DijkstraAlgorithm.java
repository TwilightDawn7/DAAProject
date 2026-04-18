package com.Manish.Project.P3;

import java.util.*;

/**
 * Dijkstra's shortest-path algorithm for the transportation network.
 *
 * Complexity: O((V + E) log V) using a min-heap (PriorityQueue).
 *
 * The graph is undirected and all weights are non-negative,
 * which satisfies Dijkstra's precondition.
 */
public class DijkstraAlgorithm {

    // Private constructor – utility class, not meant to be instantiated.
    private DijkstraAlgorithm() {}

    /**
     * Runs Dijkstra's algorithm from {@code source} over the given graph.
     *
     * @param graph  The transportation network.
     * @param source Name of the starting city.
     * @return       A {@link DijkstraResult} containing distances and paths.
     * @throws IllegalArgumentException if the source city does not exist.
     */
    public static DijkstraResult compute(TransportGraph graph, String source) {
        if (!graph.hasCity(source)) {
            throw new IllegalArgumentException("Source city not found: " + source);
        }

        Map<String, Double> dist     = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String>         visited  = new HashSet<>();

        // Initialise all distances to +∞
        for (String city : graph.getCityNames()) {
            dist.put(city, Double.POSITIVE_INFINITY);
        }
        dist.put(source, 0.0);

        // Min-heap ordered by tentative distance: [cityName, tentativeDistance]
        // We use a small helper record-like entry.
        PriorityQueue<CityEntry> pq = new PriorityQueue<>(
                Comparator.comparingDouble(e -> e.distance));

        pq.offer(new CityEntry(source, 0.0));

        while (!pq.isEmpty()) {
            CityEntry entry = pq.poll();
            String u = entry.city;

            // A city may appear multiple times in the PQ with stale distances.
            if (visited.contains(u)) continue;
            visited.add(u);

            // Relax all edges incident to u
            for (Road road : graph.getNeighborRoads(u)) {
                String v       = road.getOtherEnd(u);
                double newDist = dist.get(u) + road.getWeight();

                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    previous.put(v, u);
                    pq.offer(new CityEntry(v, newDist));
                }
            }
        }

        return new DijkstraResult(source, dist, previous);
    }

    // ─── Internal helper ──────────────────────────────────────────────────────

    /** Simple pair (city, tentative distance) used as PQ element. */
    private static class CityEntry {
        final String city;
        final double distance;

        CityEntry(String city, double distance) {
            this.city     = city;
            this.distance = distance;
        }
    }
}
