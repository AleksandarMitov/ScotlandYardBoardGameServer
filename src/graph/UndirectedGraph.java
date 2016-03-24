package graph;

import java.util.*;

/**
 * A class which represents an undirected graph. It uses a directed graph and adds each edge going both ways.
 */

public class UndirectedGraph<X, Y> extends DirectedGraph<X, Y>{

    /**
     * Constructs a new UndirectedGraph.
     */
    public UndirectedGraph() {
        super();
    }

    /**
     * Adds an edge to the graph. It adds the edge going both ways.
     *
     * @param edge the edge to add to the graph.
     */
    @Override
    public void add(Edge<X, Y> edge){
        super.add(edge);
        super.add(edge.swap());
    }
    
}
