/**
 * This class is used to relate nodes to their indegree. Comparing two of
 * these objects will compare them by their indegree, which is the behavior
 * we want in order to make a priority queue with them that has the lowest
 * indegree at the top.
 */
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
