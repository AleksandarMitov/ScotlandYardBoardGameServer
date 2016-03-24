package scotlandyard;

import graph.*;

import java.io.*;
import java.io.IOException;

public class ScotlandYardGraphReader {

    public ScotlandYardGraph readGraph(String filename) throws IOException {
        InputStream input = this.getClass().getResourceAsStream("/" + filename);
        if (input == null) {
            System.err.println("Could not load resource in ScotlandYardGraphReader: " + filename);
            System.exit(1);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        ScotlandYardGraph graph = new ScotlandYardGraph();

        String[] topLine = reader.readLine().split(" ");
        int numberOfNodes = Integer.parseInt(topLine[0]);
        int numberOfEdges = Integer.parseInt(topLine[1]);

        for (int i = 0; i < numberOfNodes; i++) {
            String line = reader.readLine();
            if (line.isEmpty()) continue;
            graph.add(getNode(line));
        }

        for (int i = 0; i < numberOfEdges; i++) {
            String line = reader.readLine();
            if (line.isEmpty()) continue;
            Edge<Integer, Transport> edge = getEdge(line, graph);
            graph.add(edge);
        }

        return graph;
    }

    private Edge<Integer, Transport> getEdge(String line, ScotlandYardGraph graph) {
        String[] parts = line.split(" ");
        Transport data = null;
        Node<Integer> n1 = null;
        Node<Integer> n2 = null;

        try {
            data = Transport.valueOf(parts[2]);
            n1 = graph.getNode(Integer.parseInt(parts[0]));
            n2 = graph.getNode(Integer.parseInt(parts[1]));
        } catch (IllegalArgumentException e) {
            System.err.println("Error reading graph. Cannot create edge for line: " + line);
            System.exit(1);
        }

        if (n1 == null || n2 == null) {
            if (n1 == null) System.err.println("Error reading graph. Cannot find node: " + parts[1]);
            if (n2 == null) System.err.println("Error reading graph. Cannot find node: " + parts[2]);
            System.exit(1);
        }

        Edge<Integer, Transport> edge = new Edge<Integer, Transport>(n1, n2, data);
        return edge;
    }

    private Node<Integer> getNode(String line) {
        Integer nodeNumber = 0;
        try {
            nodeNumber= Integer.parseInt(line);
        } catch(NumberFormatException e) {
            System.err.println("Error reading graph. Cannot create node for line: " + line);
            System.exit(1);
        }
        Node<Integer> node = new Node<Integer>(nodeNumber);
        return node;
    }

}
