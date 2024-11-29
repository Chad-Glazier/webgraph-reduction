import java.util.HashMap;

import org.jgrapht.graph.DefaultEdge;

public class SubgraphEdge extends DefaultEdge {
    /**
     * @return Returns the header that would be appropriate for writing a CSV 
     * file of these edges. Each edge can be formatted as a corresponding 
     * string with {@link #csvRow(HashMap)}.
     */
    public static String csvHeader() {
        return "from,to";
    }

    /**
     * @param nodeInfo This argument is necessary so that the edge can be writ-
     * ten as "<domain-name>,<domain-name>" instead of just using the node IDs.
     * @return Returns a string that would represent the edge in a CSV file ma-
     * tching headers given by {@link #csvHeader()}.
     */
    public String csvRow(HashMap<Integer, NodeInfo> nodeInfo) {
        return String.format("%s,%s", 
            nodeInfo.get(this.getSource()).domainName,
            nodeInfo.get(this.getTarget()).domainName
        );
    }
}
