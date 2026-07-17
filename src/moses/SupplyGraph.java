package moses;

import java.util.*;

/**
 * The "Brain" of M.O.S.E.S.
 * Uses Dijkstra's Algorithm on a weighted, directed graph to find the best
 * shipping route between locations.
 *
 *   Original feature: cheapest path by cost
 *   NOVEL FEATURE 1:  Multi-Criteria Optimization - same graph, pick Cost, Time, or Carbon
 *   NOVEL FEATURE 2:  Intelligent Re-routing - mark a route "disrupted" and the
 *                      algorithm automatically finds the next-best alternative
 */
public class SupplyGraph {
    private Map<Location, List<Route>> adjacencyList;

    public SupplyGraph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addLocation(Location location) {
        adjacencyList.putIfAbsent(location, new ArrayList<>());
    }

    public void addRoute(Location start, Location end, double cost, double timeHours,
                          double carbonKg, String mode) {
        addLocation(start);
        addLocation(end);
        adjacencyList.get(start).add(new Route(end, cost, timeHours, carbonKg, mode));
    }

    public Set<Location> getAllLocations() {
        return adjacencyList.keySet();
    }

    public List<Route> getRoutesFrom(Location location) {
        return adjacencyList.getOrDefault(location, new ArrayList<>());
    }

    // ================= NOVEL FEATURE 2: DISRUPTION / RE-ROUTING =================

    /**
     * Simulates a real-world disruption (bad weather, port strike, geopolitical event)
     * on a specific route. Once marked, Dijkstra will simply skip this edge, which
     * means any future call to findOptimalPath automatically re-routes around it -
     * this is the "Intelligent Re-routing" feature promised in the proposal.
     */
    public boolean disruptRoute(Location origin, Location destination, String mode) {
        List<Route> routes = adjacencyList.get(origin);
        if (routes == null) return false;
        for (Route r : routes) {
            if (r.getDestination().equals(destination) && r.getTransportMode().equalsIgnoreCase(mode)) {
                r.setDisrupted(true);
                return true;
            }
        }
        return false;
    }

    public void clearAllDisruptions() {
        for (List<Route> routes : adjacencyList.values()) {
            for (Route r : routes) r.setDisrupted(false);
        }
    }

    // ================= NOVEL FEATURE 1: MULTI-CRITERIA DIJKSTRA =================

    /** Result bundle: minimum weight to reach every location, plus how we got there. */
    public static class PathResult {
        public final Map<Location, Double> minWeights;
        public final Map<Location, Route> cameFrom;      // the edge used to reach each location
        public final Map<Location, Location> previousLocation; // the location before it on the path

        PathResult(Map<Location, Double> minWeights, Map<Location, Route> cameFrom,
                   Map<Location, Location> previousLocation) {
            this.minWeights = minWeights;
            this.cameFrom = cameFrom;
            this.previousLocation = previousLocation;
        }
    }

    /**
     * Dijkstra's Algorithm, generalized to optimize for Cost, Time, or Carbon,
     * and automatically skipping any route currently marked as disrupted.
     */
    public PathResult findOptimalPath(Location startNode, OptimizationCriteria criteria) {
        PriorityQueue<NodeWeight> pq = new PriorityQueue<>(Comparator.comparingDouble(nw -> nw.weight));
        Map<Location, Double> minWeights = new HashMap<>();
        Map<Location, Route> cameFrom = new HashMap<>();
        Map<Location, Location> previousLocation = new HashMap<>();

        for (Location loc : adjacencyList.keySet()) {
            minWeights.put(loc, Double.MAX_VALUE);
        }
        minWeights.put(startNode, 0.0);
        pq.add(new NodeWeight(startNode, 0.0));

        while (!pq.isEmpty()) {
            NodeWeight current = pq.poll();
            Location currentLocation = current.location;

            if (current.weight > minWeights.get(currentLocation)) continue;

            for (Route route : getRoutesFrom(currentLocation)) {
                if (route.isDisrupted()) continue; // skip blocked routes - the re-routing logic

                double newWeight = minWeights.get(currentLocation) + route.getWeightFor(criteria);
                if (newWeight < minWeights.get(route.getDestination())) {
                    minWeights.put(route.getDestination(), newWeight);
                    cameFrom.put(route.getDestination(), route);
                    previousLocation.put(route.getDestination(), currentLocation);
                    pq.add(new NodeWeight(route.getDestination(), newWeight));
                }
            }
        }
        return new PathResult(minWeights, cameFrom, previousLocation);
    }

    /** Reconstructs the actual sequence of routes taken from start to destination. */
    public List<Route> getPath(Location start, Location destination, OptimizationCriteria criteria) {
        PathResult result = findOptimalPath(start, criteria);
        List<Route> path = new LinkedList<>();

        if (result.minWeights.get(destination) == null || result.minWeights.get(destination) == Double.MAX_VALUE) {
            return path; // unreachable
        }

        Location step = destination;
        while (!step.equals(start)) {
            Route usedRoute = result.cameFrom.get(step);
            ((LinkedList<Route>) path).addFirst(usedRoute);
            step = result.previousLocation.get(step);
        }
        return path;
    }

    private static class NodeWeight {
        Location location;
        double weight;

        NodeWeight(Location location, double weight) {
            this.location = location;
            this.weight = weight;
        }
    }
}