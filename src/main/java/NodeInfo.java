public class NodeInfo {
    public int originalOutdegree;
    public int originalIndegree;
    public String domainName;
    public double relativePageRank;

    NodeInfo() {
        this.originalIndegree = 0;
        this.originalOutdegree = 0;
        this.domainName = "";
        this.relativePageRank = 0.0;
    }

    public static String csvHeader() {
        return "domain,pagerank,indegree,outdegree";
    }

    public String csvRow() {
        return String.format("%s,%f,%d,%d",
            this.domainName, this.relativePageRank,
            this.originalIndegree, this.originalOutdegree
        );
    }
}
