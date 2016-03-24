package swing.algorithms;

import scotlandyard.*;
import graph.*;

import java.util.*;
import java.io.IOException;

/**
 * A class to find the best route to take giving the best possible options
 * to change location as quickly as possible.
 */

public class Dijkstra {

    private PageRank pageRank;
    private Graph<Integer, Transport> graph;
    private List<Node<Integer>> nodes;

    /**
     * Constructs a new Dijkstra object.
     *
     * @param graphFilename the path to the file containing the graph data.
     */
    public Dijkstra(String graphFilename) {
        try {

            ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
            this.graph = graphReader.readGraph(graphFilename);
            nodes = graph.getNodes();
            pageRank = new PageRank(graph);
            pageRank.iterate(100);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Returns the route between two nodes, taking into
     * account player tickets and the rank of nodes.
     *
     * @param start the start location.
     * @param destination the destination location.
     * @param tickets the tickets for each route
     * the player holds.
     * @return the optimal route from start to destination.
     */
    public List<Integer> getRoute(int start, int destination, Map<Transport, Integer> tickets) {

        List<Node<Integer>> nodes = graph.getNodes();
        //Initialisation
        Map<Node<Integer>, Double> unvisitedNodes = new HashMap<Node<Integer>, Double>();
        Map<Node<Integer>, Double> distances = new HashMap<Node<Integer>, Double>();
        Map<Node<Integer>, Node<Integer>> previousNodes = new HashMap<Node<Integer>, Node<Integer>>();
        Node<Integer> currentNode = graph.getNode(start);

        for (Node<Integer> node : nodes) {
            if (!currentNode.getIndex().equals(node.getIndex())) {
                distances.put(node, Double.POSITIVE_INFINITY);
            } else {
                distances.put(node, 0.0);
            }
            Integer location = node.getIndex();
            try {
                unvisitedNodes.put(node, (1/pageRank.getPageRank(location)));
            } catch (Exception e) {
                System.err.println(e);
            }
            previousNodes.put(node, null);
        }
        //Search through the graph
        while (unvisitedNodes.size() > 0) {
            Node<Integer> m = minDistance(distances, unvisitedNodes);
            if (m == null) break;
            currentNode = m;
            if (currentNode.getIndex().equals(destination)) break;
            unvisitedNodes.remove(currentNode);

            step(graph, distances, unvisitedNodes, currentNode, previousNodes, tickets);
        }

        //Move backwards finding the shortest route
        List<Integer> route = new ArrayList<Integer>();
        while (previousNodes.get(currentNode) != null) {
            route.add(0, currentNode.getIndex());
            currentNode = previousNodes.get(currentNode);
        }
        route.add(0, currentNode.getIndex());
        return route;
    }

    // Perform a step in Dijkstra's algorithm.
    // @param graph the Graph containing all nodes.
    // @param distances all calculated distances.
    // @param unvisitedNodes set of unvisited nodes.
    // @param currentNode the node we are currently
    // looking at.
    // @param previousNodes map of nodes to the node
    // that they moved from.
    // @param tickets the player tickets for different
    // routes.
    private void step(Graph<Integer, Transport> graph, Map<Node<Integer>, Double> distances,
                      Map<Node<Integer>, Double> unvisitedNodes,
                      Node<Integer> currentNode,
                      Map<Node<Integer>, Node<Integer>> previousNodes,
                      Map<Transport, Integer> tickets) {
        List<Edge<Integer, Transport>> edges = graph.getEdgesFrom(currentNode);
        Double currentDistance = distances.get(currentNode);
        for (Edge<Integer, Transport> e : edges) {
            //For all neighbours
            Node<Integer> neighbour = e.getTarget();
            if (unvisitedNodes.get(neighbour) != null) {
                Transport route = e.getData();
                Integer numTickets = tickets.get(route);
                //Update distances
                Double tentativeDistance = currentDistance + (1 / (numTickets * pageRank.getPageRank(neighbour.getIndex())));
                if (tentativeDistance < distances.get(neighbour)) {
                    distances.put(neighbour, tentativeDistance);
                    previousNodes.put(neighbour, currentNode);
                }
            }
        }
    }

    // Returns the node with the minimum distance from
    // the set of unvisited nodes.
    // @param distances the current distances.
    // @param unvisitedNodes the nodes that have yet to be visited.
    // @return the minimum distance for all unvisited nodes.
    private Node<Integer> minDistance(Map<Node<Integer>, Double> distances, Map<Node<Integer>, Double> unvisitedNodes) {
        Double min = Double.POSITIVE_INFINITY;

        Node<Integer> minNode = null;
        for (Map.Entry<Node<Integer>, Double> entry : distances.entrySet()) {
            Double d = entry.getValue();
            if (Double.compare(d, min) < 0 && unvisitedNodes.containsKey(entry.getKey())) {
                min = d;
                minNode = entry.getKey();
            }
        }
        return minNode;
    }

}
