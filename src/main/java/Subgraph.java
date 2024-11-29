import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class Subgraph {
    public SimpleDirectedGraph<Integer, SubgraphEdge> subgraph;
    public HashMap<Integer, NodeInfo> nodeInfo;
    public int n;
    
    /**
     * Constructs a new, reduced subgraph in a two-step process:
     * 1. First, a subgraph of n nodes is produced from the original
     * graph based on in-degree.
     * 2. Second, the pagerank score of each node in the subgraph is
     * calculated, and the top m nodes are taken to form the final
     * subgraph.
     * 
     * @param n The size of the intermediate graph to generate. The
     * larger this is, the more accurately the final graph will resemble
     * the pagerank of the original webgraph.
     * @param m The size of the final graph.
     * @param graph The original webgraph.
     * @param transpose The transpose of {@code graph}.
     * @param nodeDomainFile a path to the associated file that relates
     * node ID's to their domain names.
     */
    Subgraph(int n, int m, ImmutableGraph graph, ImmutableGraph transpose, String nodeDomainFile) throws IOException {
        // First, we get the top n indegrees.
        NodeIterator iter = transpose.nodeIterator();
        PriorityQueue<NodeIndegree> topIndegrees = new PriorityQueue<>(n);
        while(iter.hasNext()) {
            int currentId = iter.nextInt();
            int currentIndegree = 0;
            int[] predecessors = iter.successorArray();
            int predecessorCount = iter.outdegree();
            HashSet<Integer> distinctPredecessors = new HashSet<Integer>();
            for (int i = 0; i < predecessorCount; i++) {
                distinctPredecessors.add(predecessors[i]);
            }
            distinctPredecessors.remove(currentId);
            currentIndegree = distinctPredecessors.size();
            if (topIndegrees.size() < n) {
                topIndegrees.add(new NodeIndegree(currentId, currentIndegree));
                continue;
            }
            if (currentIndegree > topIndegrees.peek().indegree) {
                topIndegrees.poll();
                topIndegrees.add(new NodeIndegree(currentId, currentIndegree));
            }
        }

        System.out.println("================ FINISHED FINDING TOP INDEGREES");

        System.out.println("Size of PQ: " + Integer.toString(topIndegrees.size()));

        // Now we construct the subgraph.
        int totalEdges = 0;

        SimpleDirectedGraph<Integer, SubgraphEdge> subgraph = new SimpleDirectedGraph<>(SubgraphEdge.class);
        while (!topIndegrees.isEmpty()) {
            subgraph.addVertex(topIndegrees.poll().id);
        }
        iter = graph.nodeIterator();
        while (iter.hasNext()) {
            int currentId = iter.nextInt();
            if (!subgraph.containsVertex(currentId)) continue;
            int[] successors = iter.successorArray();
            int successorCount = iter.outdegree();
            for (int i = 0; i < successorCount; i++) {
                if (subgraph.containsVertex(successors[i]) && successors[i] != currentId) {
                    subgraph.addEdge(currentId, successors[i]);
                    totalEdges++;
                    if (totalEdges % 1000000 == 0) System.out.println(Integer.toString(totalEdges) + " edges in the subgraph...");
                }
            }
        }

        System.out.println(Integer.toString(totalEdges) + " total edges in the subgraph.");

        System.out.println("================ FINISHED MAKING INDEGREES SUBGRAPH");

        // Now, we calculate the pagerank score and take the top m nodes.
        PageRank<Integer, SubgraphEdge> pr = new PageRank<>(subgraph);
        PriorityQueue<NodeScore> topRanks = new PriorityQueue<>(m);
        for (int currentId : subgraph.vertexSet()) {
            double currentScore = pr.getVertexScore(currentId);
            if (topRanks.size() < m) {
                topRanks.add(new NodeScore(currentId, currentScore));
                continue;
            }
            if (currentScore > topRanks.peek().score) {
                topRanks.poll();
                topRanks.add(new NodeScore(currentId, currentScore));
            }
        }

        HashSet<Integer> topRankSet = new HashSet<>(m);
        int[] includedIds = new int[m]; // this is important later.
        for (int i = 0; i < m; i++) {
            int nextSmallestRank = topRanks.poll().id;
            topRankSet.add(nextSmallestRank);
            includedIds[i] = nextSmallestRank;
        }

        System.out.println("================ FINISHED TOP RANK SUBSET");
        System.out.println("Size of set: " + Integer.toString(topRankSet.size()));

        
        // Now, we produce the subgraph by reducing the existing one.
        HashSet<Integer> originalVertices = new HashSet<>(n);
        for (Integer vertex : subgraph.vertexSet()) {
            originalVertices.add(vertex);
        }
        for (Integer vertex : originalVertices) {
            if (topRankSet.contains(vertex)) continue;
            Set<SubgraphEdge> edges = subgraph.edgesOf(vertex);
            for (SubgraphEdge edge : edges) {
                subgraph.removeEdge(edge);
            }
            subgraph.removeVertex(vertex);
        }
        this.subgraph = subgraph;

        System.out.println("================ FINISHED MAKING SUBGRAPH");

        // Next, we want to map each vertex to a domain name.
        Arrays.sort(includedIds);
        this.nodeInfo = new HashMap<Integer, NodeInfo>(m);
        BufferedReader reader = new BufferedReader(new FileReader(nodeDomainFile));
        int currentLineNumber = 0;
        for (int id : includedIds) {
            int linesToSkip = id - currentLineNumber;
            for (int i = 0; i < linesToSkip; i++) reader.readLine();
            String domainName = reader.readLine().split("\t")[1];
            currentLineNumber = id + 1;
            this.nodeInfo.put(id, new NodeInfo());
            this.nodeInfo.get(id).domainName = domainName;
            this.nodeInfo.get(id).relativePageRank = pr.getVertexScore(id);
        }
        reader.close();

        System.out.println("================ FINISHED MAPPING DOMAIN NAMES");

        // Next, we assign each vertex an outdegree, then indegree.
        iter = graph.nodeIterator();
        while (iter.hasNext()) {
            int currentId = iter.nextInt();
            if (!subgraph.containsVertex(currentId)) continue;
            int[] successors = iter.successorArray();
            int successorCount = iter.outdegree();
            HashSet<Integer> distinctSuccessors = new HashSet<>();
            for (int i = 0; i < successorCount; i++) {
                if (subgraph.containsVertex(successors[i])) {
                    distinctSuccessors.add(successors[i]);
                }
            }
            distinctSuccessors.remove(currentId);
            this.nodeInfo.get(currentId).originalOutdegree = distinctSuccessors.size();
        }

        System.out.println("================ FINISHED MAPPING OUTDEGREE");
        
        iter = transpose.nodeIterator();
        while (iter.hasNext()) {
            int currentId = iter.nextInt();
            if (!subgraph.containsVertex(currentId)) continue;
            int[] predecessors = iter.successorArray();
            int predecessorCount = iter.outdegree();
            HashSet<Integer> distinctPredecessors = new HashSet<>();
            for (int i = 0; i < predecessorCount; i++) {
                if (subgraph.containsVertex(predecessors[i])) {
                    distinctPredecessors.add(predecessors[i]);
                }
            }
            distinctPredecessors.remove(currentId);
            this.nodeInfo.get(currentId).originalIndegree = distinctPredecessors.size();
        }

        System.out.println("================ FINISHED MAPPING INDEGREE");

        this.n = m;
    }

    public void writeToFiles(String basename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(basename + "_nodes.csv"));
        Integer[] sortedVertices = new Integer[this.n];
        Iterator<Integer> iter = this.subgraph.vertexSet().iterator();
        for (int i = 0; i < this.n; i++) {
            sortedVertices[i] = iter.next();
        }
        Arrays.sort(sortedVertices, (a, b) -> Double.compare(
            this.nodeInfo.get(b).relativePageRank, this.nodeInfo.get(a).relativePageRank
        ));
        writer.write(NodeInfo.csvHeader());
        writer.newLine();
        for (int id : sortedVertices) {
            writer.write(this.nodeInfo.get(id).csvRow());
            writer.newLine();
        }
        writer.close();

        writer = new BufferedWriter(new FileWriter(basename + "_edges.csv"));
        writer.write(SubgraphEdge.csvHeader());
        writer.newLine();
        for (SubgraphEdge edge : this.subgraph.edgeSet()) {
            writer.write(edge.csvRow(this.nodeInfo));
            writer.newLine();
        }
        writer.close();
    }
}
