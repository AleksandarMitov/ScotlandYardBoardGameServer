package graph;

import java.util.List;
import java.util.ArrayList;

/**
 * A class which represents a node in a graph.
 */

public class Node<X> {

    private X index;

    /**
     * Constructs a new node with the specified index.
     *
     * @param index the index of the node in the graph.
     */
    public Node(X index) {
        this.index = index;
    }

    /**
     * Sets the index of the node in the graph.
     *
     * @param index the new index of the node in the graph.
     */
    public void setIndex(X index) {
        this.index = index;
    }

    /**
     * Returns the index of this node.
     *
     * @return the index of this node.
     */
    public X getIndex() {
        return index;
    }

    /**
     * Returns a representation of this node as a string.
     *
     * @return a representation of this node as a string.
     */
    public String toString() {
        return index.toString();
    }

}
