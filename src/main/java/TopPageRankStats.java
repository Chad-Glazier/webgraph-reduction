import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultEdge;

public class TopPageRankStats {
    public final NodeScore[] nodes;

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

        this.nodes = new NodeScore[n];
        for (int i = 0; i < n; i++) {
            this.nodes[i] = topRanks.poll();
        }
    }
}
