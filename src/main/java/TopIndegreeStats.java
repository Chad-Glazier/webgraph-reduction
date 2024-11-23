import java.util.HashSet;
import java.util.PriorityQueue;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

public class TopIndegreeStats {
    public final int[] nodeId;
    public final int[] nodeIndegree;

    TopIndegreeStats(int n, ImmutableGraph transposeGraph, boolean simpleGraph) {
        NodeIterator iter = transposeGraph.nodeIterator();
        PriorityQueue<NodeIndegree> topIndegrees = new PriorityQueue<>(n);
        while(iter.hasNext()) {
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
            if (topIndegrees.size() < n) {
                topIndegrees.add(new NodeIndegree(currentId, currentIndegree));
                continue;
            }
            if (currentIndegree > topIndegrees.peek().indegree) {
                topIndegrees.poll();
                topIndegrees.add(new NodeIndegree(currentId, currentIndegree));
            }
        }

        this.nodeId = new int[n];
        this.nodeIndegree = new int[n];
        for (int i = 0; i < n; i++) {
            NodeIndegree node = topIndegrees.poll();
            this.nodeId[i] = node.id;
            this.nodeIndegree[i] = node.indegree;
        }
    }
}
