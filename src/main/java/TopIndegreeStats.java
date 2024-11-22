import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class TopIndegreeStats {
    public final int n;
    public final int[] nodeId;
    public final int[] nodeIndegree;
    public final Set<Integer> includedNodes;

    TopIndegreeStats(int n, ImmutableGraph transposeGraph) {
        this.nodeId = new int[n];
        this.nodeIndegree = new int[n];
        this.includedNodes = new HashSet<Integer>();
        this.n = n;

        NodeIterator iter = transposeGraph.nodeIterator();
        int smallestIncludedIndegree = -1;
        while (iter.hasNext()) {
            int currentId = iter.nextInt();
            int currentIndegree = iter.outdegree();
            if (currentIndegree > smallestIncludedIndegree) {
                for (int i = 0; i < n; i++) {
                    if (this.nodeIndegree[i] < currentIndegree) {
                        this.nodeId[i] = currentId;
                        this.nodeIndegree[i] = currentIndegree;
                        smallestIncludedIndegree = this.nodeIndegree[n-1];
                        break;
                    }
                }
            }
        }
    }

    public boolean includes(int nodeId) {
        return this.includedNodes.contains(nodeId);
    }
}
