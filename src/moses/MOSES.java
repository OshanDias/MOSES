package moses;

import java.util.List;
import java.util.Map;

/**
 * Main entry point for M.O.S.E.S. (Multimodal Optimization & Supply Ecosystem Software)
 */
public class MOSES {

    public static void main(String[] args) {
        System.out.println("=== M.O.S.E.S.: Supply Chain Optimization Tool ===");

        SupplyGraph supplyChain = new SupplyGraph();

        Location shanghai  = new Location("SHA", "Shanghai Port");
        Location colombo   = new Location("CMB", "Colombo Port");
        Location rotterdam = new Location("RTM", "Rotterdam Port");
        Location london    = new Location("LHR", "London Warehouse");

        // Route(cost, timeHours, carbonKg, mode)
        supplyChain.addRoute(shanghai, colombo, 2000.0, 240, 1800, "SEA");
        supplyChain.addRoute(shanghai, colombo, 8000.0, 18, 5200, "AIR");
        supplyChain.addRoute(colombo, rotterdam, 3000.0, 288, 2400, "SEA");
        supplyChain.addRoute(rotterdam, london, 500.0, 8, 150, "LAND");
        supplyChain.addRoute(shanghai, london, 15000.0, 22, 9800, "AIR");

        // ===== Feature 1: Multi-Criteria Optimization =====
        System.out.println("\n[1] Cheapest route to London (optimize by COST):");
        printPath(supplyChain, shanghai, london, OptimizationCriteria.COST);

        System.out.println("\n[2] Fastest route to London (optimize by TIME):");
        printPath(supplyChain, shanghai, london, OptimizationCriteria.TIME);

        System.out.println("\n[3] Greenest route to London (optimize by CARBON):");
        printPath(supplyChain, shanghai, london, OptimizationCriteria.CARBON);

        // ===== Feature 2: Intelligent Re-routing =====
        System.out.println("\n[4] Simulating a disruption: Colombo -> Rotterdam SEA route is now blocked (port strike)");
        supplyChain.disruptRoute(colombo, rotterdam, "SEA");
        System.out.println("    Recalculating cheapest route to London after disruption:");
        printPath(supplyChain, shanghai, london, OptimizationCriteria.COST);

        System.out.println("\n=== Optimization Complete ===");
    }

    private static void printPath(SupplyGraph graph, Location start, Location end, OptimizationCriteria criteria) {
        List<Route> path = graph.getPath(start, end, criteria);
        if (path.isEmpty()) {
            System.out.println("    No available route.");
            return;
        }
        double totalCost = 0, totalTime = 0, totalCarbon = 0;
        System.out.print("    " + start.getName());
        for (Route leg : path) {
            System.out.print(" --[" + leg.getTransportMode() + "]--> " + leg.getDestination().getName());
            totalCost += leg.getCost();
            totalTime += leg.getTransitTimeHours();
            totalCarbon += leg.getCarbonFootprintKg();
        }
        System.out.printf("%n    Total: $%.2f | %.1f hours | %.0f kg CO2%n", totalCost, totalTime, totalCarbon);
    }
}