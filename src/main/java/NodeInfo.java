public class NodeInfo {
    public double indegreeShare;
    public String domainName;
    public double relativePageRank;

    /**
     * Constructs a new instance with no information; all fields are initialized
     * to their zero-values (numbers default to `0`, strings to `""`, etc.)
     */
    NodeInfo() {
        this.indegreeShare = 0.0;
        this.domainName = "";
        this.relativePageRank = 0.0;
    }

    /**
     * @return an appropriate CSV header to be used when writing the nodes to a
     * file. Each node can be formatted as a corresponding string with 
     * {@link #csvRow()}.
     */
    public static String csvHeader() {
        return "domain,pagerank,indegree_share";
    }

    /**
     * @return a row in a CSV file that would represent the node's information,
     * and would match the CSV headers returned by {@link #csvHeader()}.
     */
    public String csvRow() {
        return String.format("%s,%.12f,%.12f",
            this.domainName, this.relativePageRank, this.indegreeShare
        );
    }
}
