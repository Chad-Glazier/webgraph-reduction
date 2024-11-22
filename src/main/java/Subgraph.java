import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class Subgraph {
    public final int[] vertices;
    public final Hashtable<Integer, Integer> originalIndegrees;
    public final ImmutableGraph originalGraph;

    /**
     * @param nodeIds this array will be copied, and will not be mutated.
     * @param originalIndegrees this array will have the original in-degree of each node:
     * {@code nodeIds[i]} should have the original in-degree of {@code originalIndegrees[i]}.
     * @param original a reference to the original webgraph.
     */
    Subgraph(int[] nodeIds, int[] originalIndegrees, ImmutableGraph original) {
        this.originalIndegrees = new Hashtable<Integer, Integer>();
        for (int i = 0; i < nodeIds.length; i++) {
            this.originalIndegrees.put(nodeIds[i], originalIndegrees[i]);
        }
        this.vertices = Arrays.copyOf(nodeIds, nodeIds.length);
        Arrays.sort(this.vertices);
        this.originalGraph = original;
    }

    public void writeToFiles(String basename, String domainNamesFile) throws IOException {
        BufferedWriter nodeWriter = new BufferedWriter(new FileWriter(basename + "_nodes.csv"));
        nodeWriter.write("node_id,domain_name,original_simple_indegree");
        nodeWriter.newLine();
        NodeDomainMap domainNames = new NodeDomainMap(this.vertices, domainNamesFile);
        HashSet<Integer> includedIds = new HashSet<Integer>(this.vertices.length);
        for (int nodeId : this.vertices) {
            includedIds.add(nodeId);
            nodeWriter.write(String.format(
                "%d,%s,%d",
                nodeId, 
                domainNames.get(nodeId), 
                this.originalIndegrees.get(nodeId)
            ));
            nodeWriter.newLine();
        }
        nodeWriter.close();

        BufferedWriter edgeWriter = new BufferedWriter(new FileWriter(basename + "_edges.csv"));
        edgeWriter.write("from_id,to_id");
        edgeWriter.newLine();
        NodeIterator iter = this.originalGraph.nodeIterator();
        while (iter.hasNext()) {
            int currentId = iter.nextInt();
            if (!includedIds.contains(currentId))
                continue;
            int[] successors = iter.successorArray();
            int successorCount = iter.outdegree();
            HashSet<Integer> distinctSuccessors = new HashSet<Integer>(successorCount);
            for (int i = 0; i < successorCount; i++) {
                if (!includedIds.contains(successors[i]))
                    continue;
                distinctSuccessors.add(successors[i]);
            }
            for (int successor : distinctSuccessors) {
                edgeWriter.write(String.format(
                    "%d,%d", 
                    currentId,
                    successor
                ));
                edgeWriter.newLine();
            }
        }
        edgeWriter.close();
    }
}
