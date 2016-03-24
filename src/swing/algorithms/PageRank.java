package swing.algorithms;

import graph.*;
import scotlandyard.*;

import java.util.*;
import java.io.IOException;

/**
 * A class to calculate the PageRank for each of the nodes in the graph.
 */

public class PageRank {

    private Graph<Integer, Transport> graph;
    private Map<Node<Integer>, Double> pageRanks;
    private List<Node<Integer>> nodes;
    private Double d;

    /**
     * Constructs a new PageRank object.
     *
     * @param graph the graph containing the nodes.
     */
    public PageRank(Graph<Integer, Transport> graph) {
        this.graph = graph;
        pageRanks = new HashMap<Node<Integer>, Double>();
        d = new Double(0.85);
        initialiseMap();
    }

    // Initialises the Map by setting the PageRanks of all nodes to 0.
    private void initialiseMap() {
        nodes = graph.getNodes();
        for (Node<Integer> node : nodes) {
            pageRanks.put(node, new Double(0.0));
        }
    }

    /**
     * Completes n iterations of the algorithm to calculate the new
     * PageRank for all nodes.
     *
     * @param iterNo the number of iterations to perform.
     */
    public void iterate(int iterNo) {
        for (int i = 0; i < iterNo; i++) {
            iterate();
        }
    }
    /**
     * Completes one iteration of the algorithm to calculate the new
     * PageRank for all nodes.
     */
    public void iterate() {
        Map<Node<Integer>, Double> updatedPageRanks = new HashMap<Node<Integer>, Double>();
        for (Map.Entry<Node<Integer>, Double> entry : pageRanks.entrySet()) {
            Node<Integer> node = entry.getKey();
            List<Edge<Integer, Transport>> edges = graph.getEdgesFrom(node);
            Double newPageRank = (1 - d) + (d * sumPageRanks(node.getIndex(), edges));
            updatedPageRanks.put(node, newPageRank);
        }
        pageRanks = updatedPageRanks;
    }

    // Returns the sum of the PageRank's of the edges of a node.
    // @param currentLocation the current node's location.
    // @param edges the List of edges a node has.
    // @return the sum of the PageRank's of the edges of a node.
    private Double sumPageRanks(Integer currentLocation, List<Edge<Integer, Transport>> edges) {
        Double sum = new Double(0.0);
        for (Edge<Integer, Transport> edge : edges) {
            Node<Integer> location = edge.getSource();
            if (location.getIndex().equals(currentLocation)) {
                location = edge.getTarget();
            }
            Node<Integer> node = graph.getNode(location.getIndex());
            List<Edge<Integer, Transport>> nodeEdges = graph.getEdgesFrom(node);
            sum += getPageRank(node.getIndex()) / (double) nodeEdges.size();
        }
        return sum;
    }

    /**
     * Returns the PageRank for a given node.
     *
     * @param location the location of the node.
     * @return the PageRank for a given node.
     */
    public Double getPageRank(Integer location) {
        Node<Integer> node = graph.getNode(location);
        return pageRanks.get(node);
    }

}
