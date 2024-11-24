import java.io.IOException;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

public class App {
    public static void main(String[] args) {
        String[] years = {"2024", "2018"};
        for (String year : years) {
            generateReducedGraphs(
                year,
                "data/" + year, 
                "data/" + year + "-t",
                "data/" + year + "-node-domains.txt",
                7000,
                0
            );
            generateReducedGraphs(
                year,
                "data/" + year, 
                "data/" + year + "-t",
                "data/" + year + "-node-domains.txt",
                15000,
                0
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
