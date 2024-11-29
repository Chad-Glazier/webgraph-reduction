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
     * 
     * - First, a subgraph of n nodes is produced from the original
     * graph based on in-degree.
     * - Second, the pagerank score of each node in the subgraph is
     * calculated, and the top m nodes are taken to form the final
     * subgraph.
     * 
     * @param n The size of the intermediate graph to generate. The
     * larger this is, the more accurately the final graph will resemble
     * the pagerank of the original webgraph.
     * @param m The size of the final graph.
     * @param graph The original webgraph.
     * @param transpose The transpose of {@code graph}.
     * @param nodeDomainFile A path to the associated file that relates
     * node ID's to their domain names.
     * @param statsFile A path the the associated ".stats" file. This is
     * needed to find the total number of edges.
     */
    Subgraph(int n, int m, ImmutableGraph graph, ImmutableGraph transpose, String nodeDomainFile) throws IOException {        
        System.out.printf("Producing new subgraph. This may take a while.\n");

        // First, we get the top n indegrees.
        long totalIndegree = 0; // we need this later to calculate indegree share.
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
            totalIndegree += currentIndegree;
            if (topIndegrees.size() < n) {
                topIndegrees.add(new NodeIndegree(currentId, currentIndegree));
                continue;
            }
            if (currentIndegree > topIndegrees.peek().indegree) {
                topIndegrees.poll();
                topIndegrees.add(new NodeIndegree(currentId, currentIndegree));
            }
        }

        System.out.println("Finished finding the top " + topIndegrees.size() + " indegrees.");
        System.out.println("Now constructing the subgraph from those nodes:");

        // Make a map that relates the top n nodes to their in-degrees (we'll need it later).
        HashMap<Integer, Integer> indegreeMap = new HashMap<>();
        // Now we construct the subgraph.
        int totalEdges = 0;
        SimpleDirectedGraph<Integer, SubgraphEdge> subgraph = new SimpleDirectedGraph<>(SubgraphEdge.class);
        while (!topIndegrees.isEmpty()) {
            NodeIndegree node = topIndegrees.poll();
            indegreeMap.put(node.id, node.indegree);
            subgraph.addVertex(node.id);
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
                    if (totalEdges % 1000000 == 0) System.out.printf("  %d edges in the subgraph...\n", totalEdges);
                }
            }
        }

        System.out.printf("Graph complete with %d nodes and %d edges.\n", n, totalEdges);

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

        System.out.printf("Finished finding the top %d nodes by PageRank.\n", topRankSet.size());
        
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

        System.out.printf("Reduced the subgraph to %d nodes and %d edges by PageRank.\n", m, subgraph.edgeSet().size());
        System.out.printf("Now collecting metrics on nodes:\n");

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

        System.out.printf(" domain names recorded...\n");

        // Next, we assign each vertex their share of the total indegree.
        for (int id : includedIds) {
            this.nodeInfo.get(id).indegreeShare = ((double) indegreeMap.get(id)) / ((double) totalIndegree);
        }

        System.out.printf(" indegrees recorded...\n");
        System.out.printf("Graph complete with %d nodes and %d edges.\n", 
            this.subgraph.vertexSet().size(), this.subgraph.edgeSet().size()
        );
        
        this.n = m;
    }

    /**
     * Writes the graph to file by producing two CSV's: one with a list of 
     * nodes, and one with a list of edges. The headers of the nodes file will
     * be given by {@link NodeInfo#csvHeader()} and the rows by 
     * {@link NodeInfo#csvRow()}.
     * 
     * @param basename the filepath where the CSV files will be written. E.g.,
     * "outdir/graph1" will produce files in the "outdir" directory (which must
     * already exist) named "graph1_nodes.csv" and "graph1_edges.csv".
     * @throws IOException If the path is invalid (if the {@code basename} references
     * a directory which doesn't exist), then an exception is thrown.
     */
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
