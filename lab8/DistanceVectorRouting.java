import java.io.*;
import java.util.*;
import java.util.concurrent.*;


class Edge {
    String router1;
    String router2;
    int cost;
    
    public Edge(String router1, String router2, int cost) {
        this.router1 = router1;
        this.router2 = router2;
        this.cost = cost;
    }
    
    @Override
    public String toString() {
        return router1 + " <-> " + router2 + " (cost: " + cost + ")";
    }
}


class RouteEntry {
    int cost;
    String nextHop;
    
    public RouteEntry(int cost, String nextHop) {
        this.cost = cost;
        this.nextHop = nextHop;
    }
    
    @Override
    public String toString() {
        return "Cost: " + cost + ", Next: " + nextHop;
    }
}


class Router {
    private String id;
    private Map<String, Integer> neighbors; 
    private Map<String, RouteEntry> routingTable; 
    private Map<String, Integer> distanceVector; 
    private boolean tableChanged;
    
    public Router(String id) {
        this.id = id;
        this.neighbors = new HashMap<>();
        this.routingTable = new HashMap<>();
        this.distanceVector = new HashMap<>();
        this.tableChanged = false;
    }
    

    public void addNeighbor(String neighborId, int cost) {
        neighbors.put(neighborId, cost);
        routingTable.put(neighborId, new RouteEntry(cost, neighborId));
        distanceVector.put(neighborId, cost);
    }
    
    
    public void initializeRoutingTable(Set<String> allRouters) {
        
        routingTable.put(id, new RouteEntry(0, id));
        distanceVector.put(id, 0);
        
        
        for (String router : allRouters) {
            if (!routingTable.containsKey(router)) {
                routingTable.put(router, new RouteEntry(Integer.MAX_VALUE, null));
                distanceVector.put(router, Integer.MAX_VALUE);
            }
        }
    }
    
    public Map<String, Integer> getDistanceVector() {
        return new HashMap<>(distanceVector);
    }
    
    
    public Map<String, Integer> getDistanceVectorWithPoisonReverse(String neighborId) {
        Map<String, Integer> vector = new HashMap<>(distanceVector);
        
        
        for (Map.Entry<String, RouteEntry> entry : routingTable.entrySet()) {
            String dest = entry.getKey();
            RouteEntry route = entry.getValue();
            
            if (route.nextHop != null && route.nextHop.equals(neighborId) && !dest.equals(id)) {
                vector.put(dest, Integer.MAX_VALUE);
            }
        }
        
        return vector;
    }
    
    
    public boolean receiveDistanceVector(String fromNeighbor, Map<String, Integer> receivedVector) {
        boolean updated = false;
        int linkCost = neighbors.get(fromNeighbor);
        
        for (Map.Entry<String, Integer> entry : receivedVector.entrySet()) {
            String destination = entry.getKey();
            int receivedCost = entry.getValue();
            
            
            if (receivedCost == Integer.MAX_VALUE) {
                continue;
            }
            
        
            long newCost = (long) linkCost + receivedCost;
            if (newCost > Integer.MAX_VALUE) {
                newCost = Integer.MAX_VALUE;
            }
            
        
            int currentCost = distanceVector.getOrDefault(destination, Integer.MAX_VALUE);
            
            
            if (newCost < currentCost) {
                routingTable.put(destination, new RouteEntry((int) newCost, fromNeighbor));
                distanceVector.put(destination, (int) newCost);
                updated = true;
            }
        }
        
        if (updated) {
            tableChanged = true;
        }
        
        return updated;
    }
    
    public void updateNeighborCost(String neighborId, int newCost) {
        if (neighbors.containsKey(neighborId)) {
            int oldCost = neighbors.get(neighborId);
            neighbors.put(neighborId, newCost);
            
            routingTable.put(neighborId, new RouteEntry(newCost, neighborId));
            distanceVector.put(neighborId, newCost);
            
            for (Map.Entry<String, RouteEntry> entry : routingTable.entrySet()) {
                String dest = entry.getKey();
                RouteEntry route = entry.getValue();
                
                if (route.nextHop != null && route.nextHop.equals(neighborId) && !dest.equals(neighborId)) {
                    if (route.cost != Integer.MAX_VALUE) {
                        int adjustedCost = route.cost - oldCost + newCost;
                        if (adjustedCost < 0 || adjustedCost > Integer.MAX_VALUE) {
                            adjustedCost = Integer.MAX_VALUE;
                        }
                        routingTable.put(dest, new RouteEntry(adjustedCost, neighborId));
                        distanceVector.put(dest, adjustedCost);
                    }
                }
            }
            
            tableChanged = true;
        }
    }
    
    
    
    public void printRoutingTable(long currentTime) {
        System.out.println("\n[Time = " + currentTime + "s] Routing Table at Router " + id + ":");
        System.out.println("Dest | Cost | Next Hop");
        System.out.println("-----------------------");
        
        List<String> destinations = new ArrayList<>(routingTable.keySet());
        Collections.sort(destinations);
        
        for (String dest : destinations) {
            RouteEntry route = routingTable.get(dest);
            String costStr = (route.cost == Integer.MAX_VALUE) ? "∞" : String.valueOf(route.cost);
            String nextHop = (route.nextHop == null) ? "-" : route.nextHop;
            System.out.printf("%-4s | %-4s | %-8s%n", dest, costStr, nextHop);
        }
    }
    
    public String getId() { return id; }
    public Set<String> getNeighbors() { return neighbors.keySet(); }
    public boolean hasTableChanged() { return tableChanged; }
    public void resetTableChanged() { tableChanged = false; }
    public Map<String, RouteEntry> getRoutingTable() { return new HashMap<>(routingTable); }
}

public class DistanceVectorRouting {
    private Map<String, Router> routers;
    private List<String> logs;
    private int messageCount;
    private Random random;
    private ScheduledExecutorService scheduler;
    private List<Edge> edges; 
    private long startTime;
    private volatile boolean converged;
    
    public DistanceVectorRouting() {
        this.routers = new HashMap<>();
        this.logs = new ArrayList<>();
        this.messageCount = 0;
        this.random = new Random();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.edges = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.converged = false;
    }

    public void readTopology(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String router1 = parts[0];
                    String router2 = parts[1];
                    int cost = Integer.parseInt(parts[2]);
                    
                    addEdge(router1, router2, cost);
                }
            }
            
            logChange("Topology loaded from " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error reading topology file: " + e.getMessage());
        }
    }
    
    
    private void addEdge(String router1, String router2, int cost) {
        
        if (!routers.containsKey(router1)) {
            routers.put(router1, new Router(router1));
        }
        if (!routers.containsKey(router2)) {
            routers.put(router2, new Router(router2));
        }
        
        
        routers.get(router1).addNeighbor(router2, cost);
        routers.get(router2).addNeighbor(router1, cost);
        
        
        edges.add(new Edge(router1, router2, cost));
    }
    

    public void initializeRoutingTables() {
        Set<String> allRouterIds = routers.keySet();
        for (Router router : routers.values()) {
            router.initializeRoutingTable(allRouterIds);
        }
        
        logChange("Routing tables initialized for " + routers.size() + " routers");
        
        
        printAllRoutingTables(0);
    }
    

    public boolean sendDistanceVectors() {
        boolean anyUpdate = false;
        
        for (Router sender : routers.values()) {
            for (String neighborId : sender.getNeighbors()) {
                Router neighbor = routers.get(neighborId);
                Map<String, Integer> vectorWithPoisonReverse = 
                    sender.getDistanceVectorWithPoisonReverse(neighborId);
                
                boolean updated = neighbor.receiveDistanceVector(sender.getId(), 
                    vectorWithPoisonReverse);
                
                if (updated) {
                    anyUpdate = true;
                }
                
                messageCount++;
            }
        }
        
        return anyUpdate;
    }
    
    
    public void updateCostRandomly() {
        if (edges.isEmpty()) return;
        
        Edge randomEdge = edges.get(random.nextInt(edges.size()));
        
        
        int newCost = random.nextInt(10) + 1;
        int oldCost = randomEdge.cost;
        
        
            randomEdge.cost = newCost;
            
            
            routers.get(randomEdge.router1).updateNeighborCost(randomEdge.router2, newCost);
            routers.get(randomEdge.router2).updateNeighborCost(randomEdge.router1, newCost);
            
            long currentTime = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("\n[Time = " + currentTime + "s] Cost updated: " + 
                randomEdge.router1 + " <-> " + randomEdge.router2 + 
                " changed from " + oldCost + " to " + newCost);
            
            logChange("Cost update: " + randomEdge.router1 + " <-> " + randomEdge.router2 + 
                " from " + oldCost + " to " + newCost);
            
            converged = false;
        
    }
    
    public void printAllRoutingTables(long timeOffset) {
        long currentTime = (System.currentTimeMillis() - startTime) / 1000 + timeOffset;
        for (Router router : routers.values()) {
            router.printRoutingTable(currentTime);
        }
    }
    
    public boolean hasConverged() {
        for (Router router : routers.values()) {
            if (router.hasTableChanged()) {
                router.resetTableChanged();
                return false;
            }
        }
        return true;
    }
    
    public void printAllPairShortestPaths() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("FINAL ALL-PAIR SHORTEST PATHS");
        System.out.println("=".repeat(50));
        
        List<String> routerIds = new ArrayList<>(routers.keySet());
        Collections.sort(routerIds);
        
        System.out.print("From\\To");
        for (String to : routerIds) {
            System.out.printf("%8s", to);
        }
        System.out.println();
        
        for (String from : routerIds) {
            System.out.printf("%-7s", from);
            Router router = routers.get(from);
            Map<String, RouteEntry> table = router.getRoutingTable();
            
            for (String to : routerIds) {
                RouteEntry route = table.get(to);
                String cost = (route.cost == Integer.MAX_VALUE) ? "∞" : String.valueOf(route.cost);
                System.out.printf("%8s", cost);
            }
            System.out.println();
        }
    }
    
    public void logChange(String message) {
        long currentTime = (System.currentTimeMillis() - startTime) / 1000;
        String logEntry = "[" + currentTime + "s] " + message;
        logs.add(logEntry);
    }
    
    public void printStatistics() {
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("PERFORMANCE STATISTICS");
        System.out.println("=".repeat(50));
        System.out.println("Total simulation time: " + totalTime + " seconds");
        System.out.println("Total messages exchanged: " + messageCount);
        System.out.println("Number of routers: " + routers.size());
        System.out.println("Number of edges: " + edges.size());
        System.out.println("Total log entries: " + logs.size());
    }
    
    
    public void startSimulation() {
        System.out.println("Starting Distance Vector Routing Simulation...\n");
        
        
        scheduler.scheduleAtFixedRate(() -> {
            boolean updated = sendDistanceVectors();
            if (!updated) {
                System.out.println("\n[Time = " + (System.currentTimeMillis() - startTime) / 1000 + "s] " + " no changes"
                );
            }
           
            else if (updated) {
                
                long currentTime = (System.currentTimeMillis() - startTime) / 1000;
                
                
                for (Router router : routers.values()) {
                    if (router.hasTableChanged()) {
                        router.printRoutingTable(currentTime);
                    }
                }
                
                if (hasConverged()) {
                    if (!converged) {
                        System.out.println("\n[Time = " + currentTime + "s] Convergence complete. " +
                            "Total messages exchanged: " + messageCount);
                        converged = true;
                    }
                }


            }

            // if (!updated) {
            //     System.out.println("\n[Time = " + (System.currentTimeMillis() - startTime) / 1000 + "s] " + " no changes"
            //     );
            // }
        }, 5, 5, TimeUnit.SECONDS);
        
        
        scheduler.scheduleAtFixedRate(() -> {
            updateCostRandomly();
        }, 30, 30, TimeUnit.SECONDS);

        
    }
    
    
    public void stopSimulation() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
    
    
    public static void main(String[] args) {
        DistanceVectorRouting dvr = new DistanceVectorRouting();
        
        
        String topologyFile = "topology.txt";
        if (args.length > 0) {
            topologyFile = args[0];
        }
        
        System.out.println("Reading topology from: " + topologyFile);
        dvr.readTopology(topologyFile);
        
        
        if (dvr.routers.isEmpty()) {
            System.err.println("Error: No routers loaded from topology file. Please check the file exists and has correct format.");
            System.err.println("Expected format: RouterA RouterB Cost");
            System.err.println("Example: A B 2");
            return;
        }
        
        
        dvr.initializeRoutingTables();
        
    
        dvr.startSimulation();
        
        
        try {
            Thread.sleep(120000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        
        dvr.stopSimulation();
        dvr.printAllPairShortestPaths();
        dvr.printStatistics();
        
        System.out.println("\nSimulation completed.");
    }
}



