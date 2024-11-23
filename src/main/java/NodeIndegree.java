public class NodeIndegree implements Comparable<NodeIndegree> {
    public int id;
    public int indegree;

    NodeIndegree(int id, int indegree) {
        this.id = id;
        this.indegree = indegree;
    }

    @Override
    public int compareTo(NodeIndegree other) {
        return Integer.compare(this.indegree, other.indegree);
    }
}
