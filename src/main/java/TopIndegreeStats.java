import java.util.HashSet;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class TopIndegreeStats {
    public final int n;
    public final int[] nodeId;
    public final int[] nodeIndegree;

    TopIndegreeStats(int n, ImmutableGraph transposeGraph, boolean simpleGraph) {
        this.nodeId = new int[n];
        this.nodeIndegree = new int[n];
        this.n = n;

        NodeIterator iter = transposeGraph.nodeIterator();
        int smallestIncludedIndegree = -1;
        while (iter.hasNext()) {
            int currentId = iter.nextInt();
            int currentIndegree = 0;
            if (simpleGraph) {
                int[] predecessors = iter.successorArray();
                int predecessorCount = iter.outdegree();
                HashSet<Integer> distinctPredecessors = new HashSet<Integer>();
                for (int i = 0; i < predecessorCount; i++) {
                    distinctPredecessors.add(predecessors[i]);
                }
                distinctPredecessors.remove(currentId);
                currentIndegree = distinctPredecessors.size();
            } else {
                currentIndegree = iter.outdegree();
            }
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
}
