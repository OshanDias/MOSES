package moses;

/**
 * What the pathfinding algorithm should optimize for.
 * This is what makes M.O.S.E.S "multimodal optimization" rather than a single
 * fixed cheapest-path calculator - the same graph, three different best answers.
 */
public enum OptimizationCriteria {
    COST,
    TIME,
    CARBON
}
