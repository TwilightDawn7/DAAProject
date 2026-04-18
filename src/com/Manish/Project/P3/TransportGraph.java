package com.Manish.Project.P3;

import java.io.Serializable;
import java.util.*;

/**
 * Core graph data structure for the transportation network.
 *
 * Implementation: Undirected, weighted adjacency list.
 * - Cities  → HashMap<String, City>          (name → City)
 * - Roads   → HashMap<String, List<Road>>    (city name → list of incident roads)
 * - allRoads → flat List<Road>               (for MST iteration and serialization)
 *
 * A single Road object appears in BOTH endpoint lists (shared reference).
 */
public class TransportGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, City>        cities;        // ordered insertion map
    private final Map<String, List<Road>>  adjacencyList;
    private final List<Road>               allRoads;

    public TransportGraph() {
        cities        = new LinkedHashMap<>();
        adjacencyList = new LinkedHashMap<>();
        allRoads      = new ArrayList<>();
    }

    // ─── City Operations ──────────────────────────────────────────────────────

    /**
     * Adds a city. Returns false if a city with the same name already exists.
     */
    public boolean addCity(City city) {
        if (cities.containsKey(city.getName())) return false;
        cities.put(city.getName(), city);
        adjacencyList.put(city.getName(), new ArrayList<>());
        return true;
    }

    /**
     * Removes a city and all its incident roads.
     * Returns false if city not found.
     */
    public boolean removeCity(String name) {
        if (!cities.containsKey(name)) return false;

        // Remove all roads touching this city
        allRoads.removeIf(r -> r.getFrom().equals(name) || r.getTo().equals(name));
        for (List<Road> list : adjacencyList.values()) {
            list.removeIf(r -> r.getFrom().equals(name) || r.getTo().equals(name));
        }

        adjacencyList.remove(name);
        cities.remove(name);
        return true;
    }

    // ─── Road Operations ──────────────────────────────────────────────────────

    /**
     * Adds a bidirectional road between two cities with the given weight.
     * Returns false if either city is missing, endpoints are the same,
     * or the road already exists.
     */
    public boolean addRoad(String from, String to, double weight) {
        if (!cities.containsKey(from) || !cities.containsKey(to)) return false;
        if (from.equals(to)) return false;
        for (Road r : allRoads) {
            if (r.connects(from, to)) return false;   // already exists
        }
        Road road = new Road(from, to, weight);
        adjacencyList.get(from).add(road);
        adjacencyList.get(to).add(road);              // shared reference
        allRoads.add(road);
        return true;
    }

    /**
     * Removes the road between two cities (in either direction).
     * Returns false if the road does not exist.
     */
    public boolean removeRoad(String from, String to) {
        Road target = null;
        for (Road r : allRoads) {
            if (r.connects(from, to)) { target = r; break; }
        }
        if (target == null) return false;

        allRoads.remove(target);
        adjacencyList.get(from).removeIf(r -> r.connects(from, to));
        adjacencyList.get(to).removeIf(r -> r.connects(from, to));
        return true;
    }

    // ─── Query Methods ────────────────────────────────────────────────────────

    public List<Road>        getNeighborRoads(String cityName) {
        return adjacencyList.getOrDefault(cityName, Collections.emptyList());
    }

    public Collection<City>  getCities()          { return cities.values(); }
    public Set<String>       getCityNames()       { return cities.keySet(); }
    public List<Road>        getAllRoads()         { return allRoads; }
    public City              getCity(String name) { return cities.get(name); }
    public int               getCityCount()       { return cities.size(); }
    public boolean           hasCity(String name) { return cities.containsKey(name); }

    /**
     * Checks whether a road exists between the two cities.
     */
    public boolean hasRoad(String from, String to) {
        for (Road r : allRoads) if (r.connects(from, to)) return true;
        return false;
    }

    // ─── Visual State Helpers ─────────────────────────────────────────────────

    /** Resets all city and road highlight types to "normal". */
    public void clearHighlights() {
        cities.values().forEach(c -> c.setHighlightType("normal"));
        allRoads.forEach(r -> r.setHighlightType("normal"));
    }

    // ─── Serialization Support ────────────────────────────────────────────────

    /**
     * Must be called after deserialization (readObject) to rebuild the
     * adjacency list from the flat allRoads list, because transient fields
     * are not restored automatically.
     */
    public void reinitializeAdjacencyList() {
        adjacencyList.clear();
        for (String name : cities.keySet()) {
            adjacencyList.put(name, new ArrayList<>());
        }
        for (Road r : allRoads) {
            if (adjacencyList.containsKey(r.getFrom())) adjacencyList.get(r.getFrom()).add(r);
            if (adjacencyList.containsKey(r.getTo()))   adjacencyList.get(r.getTo()).add(r);
        }
    }

    @Override
    public String toString() {
        return String.format("TransportGraph[cities=%d, roads=%d]",
                cities.size(), allRoads.size());
    }
}
