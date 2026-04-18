package com.Manish.Project.P3;

import java.util.*;

/**
 * Minimum Spanning Tree via Kruskal's Algorithm with Union-Find.
 *
 * Strategy:
 *  1. Sort all edges by weight (ascending).
 *  2. Greedily add an edge if it connects two different components
 *     (determined by Union-Find with path compression + union by rank).
 *  3. Stop when V-1 edges have been added (complete spanning tree).
 *
 * Complexity: O(E log E)  – dominated by the sort step.
 */
public class MSTAlgorithm {

    private MSTAlgorithm() {}

    /**
     * Computes the MST for the given graph using Kruskal's algorithm.
     *
     * @param graph The transportation network (must have ≥ 2 cities).
     * @return      An {@link MSTResult} with the selected edges and total cost.
     */
    public static MSTResult kruskal(TransportGraph graph) {
        // ── 1. Sort all roads by weight ──────────────────────────────────────
        List<Road> sorted = new ArrayList<>(graph.getAllRoads());
        sorted.sort(Comparator.comparingDouble(Road::getWeight));

        // ── 2. Initialise Union-Find ─────────────────────────────────────────
        UnionFind uf = new UnionFind(graph.getCityNames());

        // ── 3. Greedily build MST ────────────────────────────────────────────
        List<Road> mstEdges = new ArrayList<>();
        double     totalCost = 0.0;
        int        target    = graph.getCityCount() - 1;  // edges needed

        for (Road road : sorted) {
            String rootFrom = uf.find(road.getFrom());
            String rootTo   = uf.find(road.getTo());

            if (!rootFrom.equals(rootTo)) {          // different components?
                mstEdges.add(road);
                totalCost += road.getWeight();
                uf.union(rootFrom, rootTo);

                if (mstEdges.size() == target) break; // MST complete
            }
        }

        boolean isConnected = (mstEdges.size() == target) || (graph.getCityCount() <= 1);
        return new MSTResult(mstEdges, totalCost, isConnected);
    }

    // ─── Union-Find (Disjoint Set Union) ──────────────────────────────────────

    /**
     * Path-compressed, union-by-rank Union-Find over String keys.
     */
    private static class UnionFind {
        private final Map<String, String>  parent = new HashMap<>();
        private final Map<String, Integer> rank   = new HashMap<>();

        UnionFind(Set<String> elements) {
            for (String e : elements) {
                parent.put(e, e);
                rank.put(e, 0);
            }
        }

        /** Find root with path compression. */
        String find(String x) {
            if (!parent.get(x).equals(x)) {
                parent.put(x, find(parent.get(x)));   // path compression
            }
            return parent.get(x);
        }

        /** Union by rank. */
        void union(String a, String b) {
            int ra = rank.get(a);
            int rb = rank.get(b);
            if      (ra < rb) parent.put(a, b);
            else if (ra > rb) parent.put(b, a);
            else              { parent.put(b, a); rank.put(a, ra + 1); }
        }
    }
}
