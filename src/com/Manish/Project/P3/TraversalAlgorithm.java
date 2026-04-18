package com.Manish.Project.P3;

import java.util.*;

/**
 * Graph traversal algorithms: BFS (Breadth-First Search) and DFS (Depth-First Search).
 *
 * Both return an ordered list of city names in the order they were visited.
 * Neighbours are explored in sorted (alphabetical) order for determinism.
 *
 * Only the connected component reachable from {@code source} is traversed.
 * Unreachable cities are NOT included in the result list.
 */
public class TraversalAlgorithm {

    private TraversalAlgorithm() {}

    // ─── BFS ──────────────────────────────────────────────────────────────────

    /**
     * Breadth-First Search starting from {@code source}.
     *
     * Explores level by level (closest cities first).
     * Uses a FIFO queue.
     *
     * Complexity: O(V + E)
     *
     * @return Ordered list of visited city names.
     */
    public static List<String> bfs(TransportGraph graph, String source) {
        List<String>   order   = new ArrayList<>();
        Set<String>    visited = new LinkedHashSet<>();
        Queue<String>  queue   = new LinkedList<>();

        queue.offer(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            String city = queue.poll();
            order.add(city);

            // Collect unvisited neighbours, sort for determinism
            List<String> neighbours = getSortedUnvisitedNeighbours(graph, city, visited);
            for (String nb : neighbours) {
                visited.add(nb);
                queue.offer(nb);
            }
        }
        return order;
    }

    // ─── DFS ──────────────────────────────────────────────────────────────────

    /**
     * Depth-First Search starting from {@code source}.
     *
     * Explores as deep as possible along each branch before backtracking.
     * Uses recursive implementation with an explicit visited set.
     *
     * Complexity: O(V + E)
     *
     * @return Ordered list of visited city names (pre-order).
     */
    public static List<String> dfs(TransportGraph graph, String source) {
        List<String> order   = new ArrayList<>();
        Set<String>  visited = new LinkedHashSet<>();
        dfsRecursive(graph, source, visited, order);
        return order;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static void dfsRecursive(TransportGraph graph,
                                     String city,
                                     Set<String> visited,
                                     List<String> order) {
        visited.add(city);
        order.add(city);

        List<String> neighbours = getSortedUnvisitedNeighbours(graph, city, visited);
        for (String nb : neighbours) {
            dfsRecursive(graph, nb, visited, order);
        }
    }

    /**
     * Returns neighbours of {@code city} that haven't been visited yet,
     * sorted alphabetically.
     */
    private static List<String> getSortedUnvisitedNeighbours(TransportGraph graph,
                                                             String city,
                                                             Set<String> visited) {
        List<String> neighbours = new ArrayList<>();
        for (Road road : graph.getNeighborRoads(city)) {
            String nb = road.getOtherEnd(city);
            if (!visited.contains(nb)) {
                neighbours.add(nb);
            }
        }
        Collections.sort(neighbours);
        return neighbours;
    }

    /**
     * Builds a formatted multi-line traversal summary string.
     *
     * @param order Traversal order returned by bfs() or dfs().
     * @param type  "BFS" or "DFS"
     */
    public static String formatResult(List<String> order, String source, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(type).append(" TRAVERSAL ===\n");
        sb.append("Starting from: ").append(source).append("\n\n");
        sb.append("Visit order:\n");
        for (int i = 0; i < order.size(); i++) {
            sb.append(String.format("  Step %2d: %s\n", i + 1, order.get(i)));
        }
        sb.append("\nTotal cities visited: ").append(order.size()).append("\n");
        return sb.toString();
    }
}
