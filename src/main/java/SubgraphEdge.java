import java.util.HashMap;

import org.jgrapht.graph.DefaultEdge;

public class SubgraphEdge extends DefaultEdge {
    public static String csvHeader() {
        return "from,to";
    }

    public String csvRow(HashMap<Integer, NodeInfo> nodeInfo) {
        return String.format("%s,%s", 
            nodeInfo.get(this.getSource()).domainName,
            nodeInfo.get(this.getTarget()).domainName
        );
    }
}
