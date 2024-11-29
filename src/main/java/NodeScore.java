/**
 * This class is used to relate nodes to their pagerank score. Comparing two
 * of these objects will compare them by their score, which is the behavior
 * we want in order to make a priority queue with them that has the lowest
 * score at the top.
 */
public class NodeScore implements Comparable<NodeScore> {
    public int id;
    public double score;

    NodeScore(int id, double score) {
        this.id = id;
        this.score = score;
    }

    @Override
    public int compareTo(NodeScore other) {
        return Double.compare(this.score, other.score);
    }
}
