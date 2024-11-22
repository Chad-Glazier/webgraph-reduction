import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class Subgraph {
    public final Set<Integer> vertices;
    public final ImmutableGraph original;

    Subgraph(Set<Integer> nodeIds, ImmutableGraph original) {
        this.vertices = nodeIds;
        this.original = original;

    }

    public void writeToFiles(String basename) throws IOException {
        BufferedWriter nodeWriter = new BufferedWriter(new FileWriter(basename + "_nodes.csv"));
        nodeWriter.write("node_id,original_indegree");
        nodeWriter.newLine();

        // for (node_id : this.vertices.)

        
        writer.close();
    }
}
