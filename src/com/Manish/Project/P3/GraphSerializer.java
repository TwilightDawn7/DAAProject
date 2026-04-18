package com.Manish.Project.P3;

import java.io.*;
import java.util.Collection;

/**
 * Utility class for saving and loading a {@link TransportGraph} to/from disk
 * using Java object serialization (.tng file format – Transportation Network Graph).
 */
public class GraphSerializer {

    public static final String FILE_EXTENSION = ".tng";

    private GraphSerializer() {}

    /**
     * Serialises the graph to {@code file}.
     *
     * @throws IOException if writing fails.
     */
    public static void save(TransportGraph graph, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(graph);
        }
    }

    /**
     * Deserialises a graph from {@code file} and rebuilds the adjacency list.
     *
     * @throws IOException            if reading fails.
     * @throws ClassNotFoundException if the class structure has changed.
     */
    public static TransportGraph load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            TransportGraph graph = (TransportGraph) ois.readObject();
            // Restore transient adjacency list
            graph.reinitializeAdjacencyList();
            // Restore transient highlight types
            graph.clearHighlights();
            return graph;
        }
    }

    /**
     * Exports the graph as a human-readable text summary.
     * Useful for debugging or plain-text sharing.
     */
    public static String toTextSummary(TransportGraph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Transportation Network Export ===\n\n");

        sb.append("CITIES (").append(graph.getCityCount()).append("):\n");
        for (City c : graph.getCities()) {
            sb.append(String.format("  %-18s  pos=(%d,%d)\n", c.getName(), c.getX(), c.getY()));
        }

        sb.append("\nROADS (").append(graph.getAllRoads().size()).append("):\n");
        for (Road r : graph.getAllRoads()) {
            sb.append(String.format("  %-18s <-> %-18s  %.1f km\n",
                    r.getFrom(), r.getTo(), r.getWeight()));
        }
        return sb.toString();
    }
}
