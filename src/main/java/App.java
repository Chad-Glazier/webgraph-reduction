import java.io.IOException;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

public class App {
    public static void main(String[] args) {
        String[] years = {"2018", "2024"};
        for (String year : years) {
            // generate a graph with the top 30K in-degrees, and one
            // with the top 10K pagerank scores from that subgraph.
            //
            // 30K nodes (which is about 10 million edges) is about
            // the limit of why my PC can do. You're welcome to try
            // higher numbers if you have a lot of RAM.
            generateReducedGraphs(
                year,
                "data/" + year, 
                "data/" + year + "-t",
                "data/" + year + "-node-domains.txt",
                30000,
                10000
            );
        }
    }

    public static void generateReducedGraphs(
        String year,
        String graphBasename, 
        String transposedGraphBasename,
        String domainNamesFile, 
        int sizeOfTopIndegreesSubgraph, 
        int sizeOfTopPageRankGraph
    ) {
        try {
            ImmutableGraph graph = BVGraph.loadOffline(graphBasename);
            ImmutableGraph transpose = BVGraph.loadOffline(transposedGraphBasename);
            final String outdir = "reduced_data/";
            final int n = sizeOfTopIndegreesSubgraph;
            final int m = sizeOfTopPageRankGraph;

            // Get a subgraph of top n indegrees, then write it to file.
            TopIndegreeStats topIndegrees = new TopIndegreeStats(n, transpose, true);
            Subgraph topIndegreesSubgraph = new Subgraph(topIndegrees.nodes, graph);
            topIndegreesSubgraph.writeToFiles(outdir + year + "_top_" + Integer.toString(n) + "_indegrees", domainNamesFile);

            if (sizeOfTopPageRankGraph <= 0) return; 
            
            // Convert the subgraph into a JGrapht object so that the PageRank algorithm can be run,
            // then write it to file.
            SimpleDirectedGraph<Integer, DefaultEdge> simpleDigraph = topIndegreesSubgraph.asSimpleDigraph();
            TopPageRankStats topPageRank = new TopPageRankStats(m, simpleDigraph);
            Subgraph topPageRankSubgraph = new Subgraph(topPageRank.nodes, graph);
            topPageRankSubgraph.writeToFiles(outdir + year + "_top_" + Integer.toString(m) + "_pagerank", domainNamesFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
