import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.HashMap;

import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultEdge;

public class TopPageRankStats {
    public final int[] nodeIds;
    public final double[] nodeScores;

    TopPageRankStats(int n, SimpleDirectedGraph<Integer, DefaultEdge> graph) {
        PageRank<Integer, DefaultEdge> pagerank = new PageRank<>(graph);
        Map<Integer, Double> scores = pagerank.getScores();

        PriorityQueue<NodeScore> topRanks = new PriorityQueue<>(n);
        for (int currentId : graph.vertexSet()) {
            double currentScore = scores.get(currentId);
            if (topRanks.size() < n) {
                topRanks.add(new NodeScore(currentId, currentScore));
                continue;
            }
            if (currentScore > topRanks.peek().score) {
                topRanks.poll();
                topRanks.add(new NodeScore(currentId, currentScore));
            }
        }

        this.nodeIds = new int[n];
        this.nodeScores = new double[n];
        for (int i = 0; i < n; i++) {
            NodeScore node = topRanks.poll();
            this.nodeIds[i] = node.id;
            this.nodeScores[i] = node.score;
        }
    }
}
