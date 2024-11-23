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
