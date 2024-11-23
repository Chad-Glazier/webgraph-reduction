import java.io.IOException;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

public class App {
    public static void main(String[] args) {
        try {
            final ImmutableGraph graph2017 = BVGraph.loadOffline("data/2017");
            final ImmutableGraph transposeGraph2017 = BVGraph.loadOffline("data/2017-t");
            // final ImmutableGraph graph2024 = BVGraph.loadOffline("data/2024");
            // final ImmutableGraph transposeGraph2024 = BVGraph.loadOffline("data/2024-t");
            final String outdir = "reduced_data/";

            final int n = 10000;
            TopIndegreeStats top = new TopIndegreeStats(n, transposeGraph2017, true);
            Subgraph sub = new Subgraph(top.nodeId, top.nodeIndegree, graph2017);
            sub.writeToFiles(outdir + "2017_top_" + Integer.toString(n), "data/2017-node-domains.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
