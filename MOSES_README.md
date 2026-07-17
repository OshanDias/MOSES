# M.O.S.E.S. — Multimodal Optimization & Supply Ecosystem Software

A Java desktop tool that models a global shipping network as a **weighted, directed graph** and finds the optimal route between two locations — by cost, time, or carbon footprint — using Dijkstra's Algorithm. Also simulates real-time disruptions and automatic re-routing.

Built for the Programming Data Structures and Algorithms (PDSA) module, HND in Software Engineering.

## The Problem

Global logistics planning is rarely just "find the cheapest route." A shipment might need the fastest route, the greenest route, or a new route entirely if a port is suddenly disrupted. Static route calculators can't answer all three.

## Features

- **Multi-Criteria Optimization** — the same graph, optimized by Cost, Time, or Carbon footprint (three genuinely different best routes)
- **Intelligent Re-routing** — mark any route as disrupted (e.g. a port strike) and Dijkstra automatically recalculates the next-best path
- **Multimodal Transport** — routes combine Sea, Air, and Land legs
- **Interactive Dashboard** — live route table, criteria selector, disruption simulator

## Data Structure

Weighted, directed graph, implemented as an adjacency list: `HashMap<Location, List<Route>>`. Each `Route` carries three separate weights (cost, time, carbon), and Dijkstra's Algorithm is generalized to optimize against whichever one the user selects.

## Tech Stack

Java SE 21 · Java Swing · NetBeans IDE

## How to Run

**Console demo** (shows cost/time/carbon-optimal routes, then a disruption + re-route):
```
Run: moses.MOSES
```

**GUI Dashboard**:
```
Run: moses.MOSESDashboard
```
In NetBeans: right-click `MOSESDashboard.java` → Run File.

## Screenshots

_Add a screenshot of the dashboard here after running it._

## Project Structure

```
src/moses/
├── Location.java              - a logistics hub (graph node)
├── Route.java                  - a transport link with cost/time/carbon weights (graph edge)
├── OptimizationCriteria.java   - enum: COST, TIME, CARBON
├── SupplyGraph.java            - the graph + generalized Dijkstra + disruption/re-routing logic
├── MOSES.java                  - console entry point / demo
└── MOSESDashboard.java         - Swing GUI
```
