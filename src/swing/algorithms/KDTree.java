package swing.algorithms;

import scotlandyard.*;

import java.awt.Point;
import java.io.*;
import java.util.*;

/**
 * A class to find the Node that a user clicks on.
 */

public class KDTree {
    boolean debug = false;

    private TreeNode treeRoot = null;
    private List<TreeNode> nodes;

    /**
     * Constructs a new KDTree object with the positions in the file passed in.
     *
     * @param path the path to the text file containg the pixel positions of
     * the nodes in the map.
     */
    public KDTree(String path) {
        Scanner in = null;
        nodes = new ArrayList<TreeNode>();
        try {
            in = new Scanner(this.getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Error getting graph position file :" + e);
            System.exit(1);
        }
        if (in != null) readTree(in);
    }

    // Read tree file and extract data array from it.
    // @param scanner the scanner object used for reading the file.
    private void readTree(Scanner scanner) {
        treeRoot = null;
        String topLine = scanner.nextLine();
        int numberOfNodes = Integer.parseInt(topLine);
        for (int i = 0; i < numberOfNodes; i++) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int loc = Integer.parseInt(parts[0]);
            nodes.add(new TreeNode(x, y, loc));
        }

        treeRoot = createTree(nodes, 1, null);
        scanner.close();
    }

    // Create a balanced Tree. First sort to find the median, then create the subtrees.
    // @param nodes the List of nodes in the tree.
    // @param direction the axis to be comparing in the tree.
    // @param parent the parent node in the tree.
    private TreeNode createTree(List<TreeNode> nodes, int direction, TreeNode parent) {
        if (nodes.size() == 0) return null;
        if (direction == 1) Collections.sort(nodes, new NodeXComparator());
        else if (direction == -1) Collections.sort(nodes, new NodeYComparator());
        int median = nodes.size()/2;
        TreeNode m = nodes.get(median);
        m.direction = direction;
        m.parent = parent;
        TreeNode a;
        if (nodes.size() <= 1) a = createTree(new ArrayList<TreeNode>(), -direction, m);
        else a = (createTree(nodes.subList(0, median), -direction, m));
        TreeNode b;
        if (nodes.size() <= 2) b = createTree(new ArrayList<TreeNode>(), -direction, m);
        else b = createTree(nodes.subList(median + 1, nodes.size()), -direction, m);
        m.left = a;
        m.right = b;
        return m;
    }

    /**
     * Returns the pixel coordinates of a node with a specified location.
     *
     * @param location the location of the node whose coordinates are to be
     * returned.
     * @return the coordinates of the node.
     */
    public Point getNodeLocation(int location) {
        for (TreeNode node : nodes) {
            if (node.location == location) return new Point(node.x, node.y);
        }
        return null;
    }

    /**
     * Returns the location of the node that is closest to the pixel
     * coordinates passed in.
     *
     * @param x the x coordinate to be checked against.
     * @param y the y coordinate to be checked against.
     * @return location of the closest node.
     */
    public int getNode(int x, int y) {
        int n = findNearest(treeRoot, x, y, treeRoot).location;
        return n;
    }

    // Returns whether the radius between the current best node and the checking
    // position intersects the hyperplane of the current node.
    // If dir == 1, check x.
    // @param currentBest the current best node.
    // @param currentNode the current node being checked.
    // @param x the x coordinate being checked against.
    // @param y the y coordinate being checked against.
    // @return true if the radius of the current best intersects the hyperplane of
    // current node, false otherwise.
    private boolean intersects(TreeNode currentBest, TreeNode currentNode, int x, int y) {
        double radius = Math.sqrt(Math.pow(currentBest.x - x, 2) + Math.pow(currentBest.y - y, 2));
        int dir = currentNode.direction;
        if (dir == 1){
            if (Math.abs(currentNode.x - x) < radius) return true;
        } else {
            if (Math.abs(currentNode.y - y) < radius) return true;
        }
        return false;
    }

    // Returns the inital closest node by traversing the tree.
    // @param root the root node of the tree.
    // @param x the x coordinate to be checked against.
    // @param y the y coordinate to be checked against.
    // @return the node in the tree that is initially closest to the coordinates.
    private TreeNode search(TreeNode root, int x, int y) {
        int dir = root.direction;
        if (((dir == 1 && root.x > x) || (dir == -1 && root.y > y)) && root.left != null) {
            return search(root.left, x, y);
        } else if (((dir == 1 && root.x < x) || (dir == -1 && root.y < y)) && root.right != null) {
            return search(root.right, x, y);
        }else {
            return root;
        }
    }

    // Returns the nearest node to the coordinates.
    // Search down the tree and then perform step.
    // @param tree the root node in the tree.
    // @param x the x coordinate to be checked against.
    // @param y the y coordinate to be checked against.
    // @param root the root node in the tree.
    // @return the nearest node to the coordinates.
    private TreeNode findNearest(TreeNode tree, int x, int y, TreeNode root) {
        TreeNode currentBest = search(tree, x, y);
        return step(currentBest, currentBest, x,  y, currentBest, root);

    }

    // Returns the nearest node from two potential nodes.
    // @param currentBest the current best node.
    // @param potentialBest the node to be comparing against.
    // @param x the x coordinate to be checking against.
    // @param y the y coordinate to be checking against.
    // @return the node that is nearest to the coordinates of the two.
    private TreeNode nearestNode(TreeNode currentBest, TreeNode potentialBest, int x, int y) {
        double currentDist = Math.sqrt(Math.pow(x - currentBest.x, 2) + Math.pow(y - currentBest.y, 2));
        double newDist = Math.sqrt(Math.pow(x - potentialBest.x, 2) + Math.pow(y - potentialBest.y, 2));

        if (newDist < currentDist) return potentialBest;
        else return currentBest;
    }

    // If the current best intersects current node, we apply the
    // whole search algorithm to it subtrees, picking the closest result.
    // Then we move up to the parent.
    // @param currentBest the current best node.
    // @param currentNode the current selected node.
    // @param x the x coordinate to be checking against.
    // @param y the y coordinate to be checking against.
    // @param sender the parent node.
    // @param root the root node of the tree.
    // @return the closest node to the coordinates.
    private TreeNode step(TreeNode currentBest, TreeNode currentNode, int x, int y, TreeNode sender, TreeNode root) {
        if (currentNode.equals(root)) return currentBest;
        currentBest = nearestNode(currentBest, currentNode, x, y);

        if (intersects(currentBest, currentNode, x, y)) {
            if (currentNode.left != null && currentNode.left != sender) {currentBest = nearestNode(currentBest, findNearest(currentNode.left, x, y, currentNode), x, y);}
            currentBest = nearestNode(currentBest, currentNode, x, y);
            if (currentNode.right != null && currentNode.right != sender) {currentBest = nearestNode(currentBest, findNearest(currentNode.right, x, y, currentNode), x, y);}
            currentBest = nearestNode(currentBest, currentNode, x, y);
        }
        if (sender == currentNode.parent) {return currentBest;}
        return step(currentBest, currentNode.parent, x, y, currentNode, root);
    }

    // A class to represent a node in the 2-D tree.
    private class TreeNode {

        public final int x;
        public final int y;
        public TreeNode left;
        public TreeNode right;
        public TreeNode parent;
        public final int location;
        public int direction;

        /**
         * Constructs a new TreeNode.
         *
         * @param x the x coordinate of the node.
         * @param y the y coordinate of the node.
         * @param location the location associated with the node.
         */
        public TreeNode(int x, int y, int location) {
            this.x = x;
            this.y = y;
            this.left = null;
            this.right = null;
            this.parent = null;
            this.location = location;
            this.direction = 0;
        }

        /**
         * Sets the left leaf node of this node.
         *
         * @param node the node to be set.
         */
        public void setLeft(TreeNode node) {
            this.left = node;
        }

        /**
         * Sets the right leaf node of this node.
         *
         * @param node the node to be set.
         */
        public void setRight(TreeNode node) {
            this.right = node;
        }

        /**
         * Sets the parent node to this node.
         *
         * @param node the node to be set.
         */
        public void setParent(TreeNode node) {

            this.parent = node;
        }

        /**
         * Sets the direction of this node.
         * (i.e. the axis to be checked against.)
         *
         * @param direction the direction of the node.
         */
        public void setDirection(int direction) {
            this.direction = direction;
        }
    }

    // Comparator for sorting TreeNodes in x axis
    private class NodeXComparator implements Comparator<TreeNode> {
        public int compare(TreeNode x1, TreeNode x2) {
            return (x1.x - x2.x);
        }
    }

    // Comparator for sorting TreeNodes in y axis
    private class NodeYComparator implements Comparator<TreeNode> {
        public int compare(TreeNode x1, TreeNode x2) {
            return (x1.y - x2.y);
        }
    }
}
