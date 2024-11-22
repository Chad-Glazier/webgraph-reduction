import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class App {
    public static void main(String[] args) {
        try {
            ImmutableGraph graph2017 = BVGraph.loadOffline("2017");
            ImmutableGraph transposeGraph2017 = BVGraph.loadOffline("2017-t");
            ImmutableGraph graph2024 = BVGraph.loadOffline("2024");
            ImmutableGraph transposeGraph2024 = BVGraph.loadOffline("2024-t");
            final int n = 5000;

            String filePath = "node_indegrees.csv"; // Specify the file path

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            String header = "node_index,total_indegree";
            writer.write(header);
            writer.newLine();

            for (int i = 0; i < n; i++) {
                String line = String.format("%d,%d", topIndegrees[i][0], topIndegrees[i][1]);
                writer.write(line);
                writer.newLine();
            }

            writer.close();

            // switching to the main graph

            Set<Integer> includedNodes = new HashSet<>();

            writer = new BufferedWriter(new FileWriter("nodes.csv"));
            writer.write("node");
            writer.newLine();
            for (int i = 0; i < n; i++) {
                includedNodes.add(topIndegrees[i][0]);
                writer.write(String.format("%d", topIndegrees[i][0]));
                writer.newLine();
            }
            writer.close();

            iter = graph.nodeIterator();

            BufferedWriter edgesWriter = new BufferedWriter(new FileWriter("edges.csv"));
            header = "from,to,multiplicity";
            edgesWriter.write(header);
            edgesWriter.newLine();

            while (iter.hasNext()) {
                int current = iter.nextInt();
                if (!includedNodes.contains(current))
                    continue;
                Multiset<Integer> distinctSuccessors = HashMultiset.create();
                int[] successors = iter.successorArray();
                for (int successor : successors) {
                    if (!includedNodes.contains(successor))
                        continue;
                    distinctSuccessors.add(successor);
                }
                
                for (Multiset.Entry<Integer> successor : distinctSuccessors.entrySet()) {
                    edgesWriter.write(String.format(
                        "%d,%d,%d", 
                        current,
                        successor.getElement(),
                        successor.getCount()
                    ));
                    edgesWriter.newLine();
                }
            }
            edgesWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the top {@code n} nodes with the highest in-degree.
     * 
     * @param n is the number of top indegrees to get. it's recommended that 
     * you keep this value less than or equal to ten thousand to keep the runtime 
     * reasonable.
     * @param transposeGraph is a string that represents the base filename of
     * the transposed graph. E.g., if you have the files "bvgraph-t.graph",
     * "bvgraph-t.offsets", and "bvgraph-t.properties", this argument should
     * be `"bvgraph-t"`.
     * @param outputFilename determines the name of the CSV file to be produced. If
     * you don't want to make a file for the output, pass {@code null} for this argument.
     * @return an {@code n} by {@code 2} array {@code A}, where {@code A[i][0]} is the 
     * node's index in the original graph, and {@code A[i][1]} is the node's indegree. 
     * If there is an error reading the graph, it will return {@code null}. The array
     * will be sorted in descending order of in-degree.
     */
    private static int[][] topIndegrees(int n, ImmutableGraph transposeGraph, String outputFilename) {
        NodeIterator iter = transposeGraph.nodeIterator();

        int[][] topIndegrees = new int[n][2];
        int lowestValue = -1;

        while (iter.hasNext()) {
            int current = iter.nextInt();
            int currentIndegree = iter.outdegree();

            if (currentIndegree > lowestValue) {
                for (int i = 0; i < n; i++) {
                    if (topIndegrees[i][1] < currentIndegree) {
                        topIndegrees[i][0] = current;
                        topIndegrees[i][1] = currentIndegree;
                        lowestValue = topIndegrees[n-1][1];
                        break;
                    }
                }
            }
        }

        if (outputFilename != null) try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename + ".csv"));
            writer.write("node_index,total_indegree");
            writer.newLine();
            for (int i = 0; i < n; i++) {
                writer.write(String.format("%d,%d", topIndegrees[i][0], topIndegrees[i][1]));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing output to " + outputFilename + ".csv");
        }

        return topIndegrees;
    }
}
