import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class Subgraph {
    public final NodeScore[] nodeScores;
    public final NodeIndegree[] nodeIndegrees;
    public final ImmutableGraph originalGraph;
    private final String metricDescription;

    Subgraph(NodeScore[] nodeScores, ImmutableGraph original) {
        this.nodeScores = Arrays.copyOf(nodeScores, nodeScores.length);
        Arrays.sort(this.nodeScores, (a, b) -> Integer.compare(a.id, b.id));
        this.nodeIndegrees = null;
        this.originalGraph = original;
        this.metricDescription = "pagerank_score";
    }

    Subgraph(NodeIndegree[] nodeIndegrees, ImmutableGraph original) {
        this.nodeIndegrees = Arrays.copyOf(nodeIndegrees, nodeIndegrees.length);
        Arrays.sort(this.nodeIndegrees, (a, b) -> Integer.compare(a.id, b.id));
        this.nodeScores = null;
        this.originalGraph = original;
        this.metricDescription = "original_simple_indegree";
    }

    public int[] nodeIds() {
        int[] includedIds;
        if (this.nodeIndegrees != null) {
            includedIds = new int[this.nodeIndegrees.length];
            for (int i = 0; i < this.nodeIndegrees.length; i++) {
                includedIds[i] = this.nodeIndegrees[i].id;
            }
        } else {
            // based on the constructors, if this.nodeIndegrees == null then
            // this.nodeScores is not.
            includedIds = new int[this.nodeScores.length];
            for (int i = 0; i < this.nodeScores.length; i++) {
                includedIds[i] = this.nodeScores[i].id;
            }
        }
        return includedIds;
    }

    public void writeToFiles(String basename, String domainNamesFile) throws IOException {
        int[] nodeIds = this.nodeIds();
        NodeDomainMap domainNames = new NodeDomainMap(nodeIds, domainNamesFile);

        BufferedWriter statsWriter = new BufferedWriter(new FileWriter(basename + "_stats.txt"));
        statsWriter.write(String.format("total_nodes=%d", nodeIds.length));
        statsWriter.newLine();

        BufferedWriter nodeWriter = new BufferedWriter(new FileWriter(basename + "_nodes.csv"));
        nodeWriter.write("domain_name," + this.metricDescription);
        nodeWriter.newLine();
        HashSet<Integer> includedIds = new HashSet<Integer>(nodeIds.length);
        if (this.nodeIndegrees != null) {
            // sort based on indegree (descending)
            Arrays.sort(this.nodeIndegrees, (a, b) -> Integer.compare(b.indegree, a.indegree));
            for (NodeIndegree node : this.nodeIndegrees) {
                includedIds.add(node.id);
                nodeWriter.write(String.format(
                        "%s,%d",
                        domainNames.get(node.id),
                        node.indegree));
                nodeWriter.newLine();
            }
            // restore original ordering based on id
            Arrays.sort(this.nodeIndegrees, (a, b) -> Integer.compare(a.id, b.id));
        } else {
            // sort based on score (descending)
            Arrays.sort(this.nodeScores, (a, b) -> Double.compare(b.score, a.score));
            for (NodeScore node : this.nodeScores) {
                includedIds.add(node.id);
                nodeWriter.write(String.format(
                        "%s,%f",
                        domainNames.get(node.id),
                        node.score));
                nodeWriter.newLine();
            }
            // restore original ordering based on id
            Arrays.sort(this.nodeScores, (a, b) -> Integer.compare(a.id, b.id));
        }
        nodeWriter.close();

        int totalEdgeCount = 0;

        BufferedWriter edgeWriter = new BufferedWriter(new FileWriter(basename + "_edges.csv"));
        edgeWriter.write("from_domain,to_domain");
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
                        "%s,%s",
                        domainNames.get(currentId),
                        domainNames.get(successor)));
                edgeWriter.newLine();
                totalEdgeCount++;
            }
        }
        edgeWriter.close();

        statsWriter.write(String.format("total_edges=%d", totalEdgeCount));
        statsWriter.newLine();
        double density = ((double) totalEdgeCount) / ((double) nodeIds.length * (nodeIds.length - 1f));
        statsWriter.write(String.format("edge_density=%f", density));
        statsWriter.newLine();
        statsWriter.close();
    }

    public SimpleDirectedGraph<Integer, DefaultEdge> asSimpleDigraph() {
        SimpleDirectedGraph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

        if (this.nodeIndegrees != null) {
            for (NodeIndegree node : this.nodeIndegrees) {
                graph.addVertex(node.id);
            }
        } else {
            // based on the constructors, if this.nodeIndegrees == null then
            // this.nodeScores is not.
            for (NodeScore node : this.nodeScores) {
                graph.addVertex(node.id);
            }
        }

        NodeIterator iter = this.originalGraph.nodeIterator();
        while (iter.hasNext()) {
            int currentId = iter.nextInt();
            if (!graph.containsVertex(currentId))
                continue;
            HashSet<Integer> distinctSuccessors = new HashSet<>();
            int[] successors = iter.successorArray();
            int successorCount = iter.outdegree();
            for (int i = 0; i < successorCount; i++) {
                if (graph.containsVertex(successors[i])) {
                    distinctSuccessors.add(successors[i]);
                }
            }
            distinctSuccessors.remove(currentId);
            for (int distinctSuccessor : distinctSuccessors) {
                graph.addEdge(currentId, distinctSuccessor);
            }
        }

        return graph;
    }
}
