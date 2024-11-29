import java.io.IOException;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

public class App {
    public static void main(String[] args) {
        String[] years = { "2018", "2024" };
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
                    10000);
        }
    }

    public static void generateReducedGraphs(
            String year,
            String graphBasename,
            String transposedGraphBasename,
            String domainNamesFile,
            int sizeOfTopIndegreesSubgraph,
            int sizeOfTopPageRankGraph) {
        try {
            ImmutableGraph graph = BVGraph.loadOffline(graphBasename);
            ImmutableGraph transpose = BVGraph.loadOffline(transposedGraphBasename);
            final String outdir = "reduced_data/";
            final int n = sizeOfTopIndegreesSubgraph;
            final int m = sizeOfTopPageRankGraph;

            Subgraph subgraph = new Subgraph(n, m, graph, transpose, domainNamesFile);
            subgraph.writeToFiles(outdir + String.format("%s_top_%d", year, m));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
