package graph;

/**
 * A class which represents an edge in a graph.
 */

public class Edge<X, Y> {

    private Node<X> source;
    private Node<X> target;
    private Y data;

    /**
     * Constructs a new edge between the specified nodes and with the
     * specified data.
     *
     * @param source the source node of the edge.
     * @param target the target node of the edge.
     * @param data the data of the edge.
     */
    public Edge(Node<X> source, Node<X> target, Y data) {
        this.source = source;
        this.target = target;
        this.data = data;
    }

    /**
     * Sets the data of the edge.
     *
     * @param data the new data of the edge.
     */
    public void setData(Y data) {
        this.data = data;
    }

    /**
     * Returns the source node of the edge.
     *
     * @return the source node of the edge.
     */
    public Node<X> getSource() {
        return source;
    }

    /**
     * Returns the target node of the edge.
     *
     * @return the target node of the edge.
     */
    public Node<X> getTarget() {
        return target;
    }

    /**
     * Returns the data associated with the edge.
     *
     * @return the data associated with the edge.
     */
    public Y getData() {
        return data;
    }

    /**
     * Returns the node in the edge that is not the specified node.
     * Returns null if the specified node is not in the edge.
     *
     * @param n the other node in the edge.
     * @return the node in the edge that is not the specified node.
     */
    public Node<X> other(Node<X> n) {
        if (source.equals(n)) {
            return target;
        } else if (target.equals(n)) {
            return source;
        }
        return null;
    }

    /**
     * Returns a new edge with the target and source the other way round.
     *
     * @return a new edge with the target and source the other way round.
     */
    public Edge<X, Y> swap() {
        return new Edge<X, Y>(target, source, data);
    }

    /**
     * Returns a representation of the edge as a string.
     *
     * @return a representation of the edge as a string.
     */
    public String toString() {
        return source.toString() + " " + target.toString() + " "
                + data.toString();
    }

}
