package graph;

import java.util.*;

/**
 * An interface which defines the methods needed for a graph implementation.
 */

public interface Graph<X, Y> {

    /**
     * Adds a node to the graph. Duplicate nodes are not allowed.
     *
     * @param node the node to be added to the graph.
     */
    public void add(Node<X> node);

    /**
     * Adds an edge to the graph. Multiple edges with the same source and
     * target nodes are allowed.
     *
     * @param edge the edge to add to the graph.
     */
    public void add(Edge<X, Y> edge);

    /**
     * Returns all edges in the graph.
     *
     * @return all edges in the graph.
     */
    public List<Edge<X, Y>> getEdges();

    /**
     * Returns all nodes in the graph.
     *
     * @return all nodes in the graph.
     */
    public List<Node<X>> getNodes();

    /**
     * Returns the node in the graph with the specified index. Returns null
     * if there isn't a node with that index in the graph.
     *
     * @param index the index of the node to be returned.
     * @return the node in the graph with the specified index.
     */
    public Node<X> getNode(X index);

    /**
     * Returns all edges in the graph where the target of the edge is
     * the specified node. Returns null if there aren't any edges.
     *
     * @param node the target node of the edges to be returned.
     * @return all edges in the graph where the target of the edge is
     * the specified node.
     */
    public List<Edge<X, Y>> getEdgesTo(Node<X> node);

    /**
     * Returns all edges in the graph where the source of the edge is
     * the specified node. Returns null if there aren't any edges.
     *
     * @param node the source node of the edges to be returned.
     * @return all edges in the graph where the source of the edge is
     * the specified node.
     */
    public List<Edge<X, Y>> getEdgesFrom(Node<X> node);

    /**
     * Returns a representation of the graph as a string.
     *
     * @return a representation of the graph as a string.
     */
    public String toString();

}
