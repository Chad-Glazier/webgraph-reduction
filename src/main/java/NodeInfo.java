public class NodeInfo {
    public int originalOutdegree;
    public int originalIndegree;
    public String domainName;
    public double relativePageRank;

    /**
     * Constructs a new instance with no information; all fields are initialized
     * to their zero-values (numbers default to `0`, strings to `""`, etc.)
     */
    NodeInfo() {
        this.originalIndegree = 0;
        this.originalOutdegree = 0;
        this.domainName = "";
        this.relativePageRank = 0.0;
    }

    /**
     * @return an appropriate CSV header to be used when writing the nodes to a
     * file. Each node can be formatted as a corresponding string with 
     * {@link #csvRow()}.
     */
    public static String csvHeader() {
        return "domain,pagerank,indegree,outdegree";
    }

    /**
     * @return a row in a CSV file that would represent the node's information,
     * and would match the CSV headers returned by {@link #csvHeader()}.
     */
    public String csvRow() {
        return String.format("%s,%f,%d,%d",
            this.domainName, this.relativePageRank,
            this.originalIndegree, this.originalOutdegree
        );
    }
}
