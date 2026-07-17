package moses;

/**
 * Represents a transportation link (an "Edge" with weights) between two hubs.
 * Now carries three separate weights (cost, time, carbon) instead of just one,
 * so the graph can be optimized by whichever criteria the user cares about -
 * this is what makes it genuinely "multimodal optimization" rather than just
 * a single shortest-path calculator.
 */
public class Route {
    private Location destination;
    private double cost;               // in USD
    private double transitTimeHours;   // how long this leg takes
    private double carbonFootprintKg;  // estimated CO2 for this leg
    private String transportMode;      // SEA, AIR, LAND
    private boolean disrupted;         // true if this route is currently blocked (weather, strikes, etc.)

    public Route(Location destination, double cost, double transitTimeHours,
                 double carbonFootprintKg, String transportMode) {
        this.destination = destination;
        this.cost = cost;
        this.transitTimeHours = transitTimeHours;
        this.carbonFootprintKg = carbonFootprintKg;
        this.transportMode = transportMode;
        this.disrupted = false;
    }

    public Location getDestination() { return destination; }
    public double getCost() { return cost; }
    public double getTransitTimeHours() { return transitTimeHours; }
    public double getCarbonFootprintKg() { return carbonFootprintKg; }
    public String getTransportMode() { return transportMode; }
    public boolean isDisrupted() { return disrupted; }
    public void setDisrupted(boolean disrupted) { this.disrupted = disrupted; }

    /** Returns the weight to use for a given optimization criteria. */
    public double getWeightFor(OptimizationCriteria criteria) {
        switch (criteria) {
            case TIME: return transitTimeHours;
            case CARBON: return carbonFootprintKg;
            case COST:
            default: return cost;
        }
    }

    @Override
    public String toString() {
        String status = disrupted ? " [DISRUPTED]" : "";
        return String.format("-> %s via %s ($%.2f, %.1fh, %.0fkg CO2)%s",
                destination.getName(), transportMode, cost, transitTimeHours, carbonFootprintKg, status);
    }
}