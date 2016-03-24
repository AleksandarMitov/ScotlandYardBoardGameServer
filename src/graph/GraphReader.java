package graph;

import java.io.IOException;

/**
 * An abstract class to define the methods needed to read a graph from a datasource.
 */

public abstract class GraphReader<X, Y> {

    /**
     * Returns a graph that is read from a datasource using the specified filename.
     *
     * @param filename the filename used to read the graph.
     * @return a graph that is read from a datasource using the specified filename.
     * @throws IOException if there is a problem reading in the file.
     */
    abstract public Graph<X, Y> readGraph(String filename) throws IOException;

}
