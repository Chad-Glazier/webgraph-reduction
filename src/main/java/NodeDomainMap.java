import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

public class NodeDomainMap {
    private Hashtable<Integer, String> map;
    
    /**
     * 
     * @param sortedNodeIds a sorted array of integers representing node IDs. It's
     * important that this array be sorted, otherwise the function will not produce
     * correct output.
     * @param nodeDomainFile a string representing the path to a text file that
     * contains all domain names of each node ID. Counting from zero, the i-th line 
     * of this file should have the format {@code<integer><tab><string>} where the 
     * integer is i (the node's ID) and the string is the associated domain name.
     * @throws IOException if the node domain file isn't found or is malformed.
     */
    NodeDomainMap(int[] sortedNodeIds, String nodeDomainFile) throws IOException {
        this.map = new Hashtable<Integer, String>(sortedNodeIds.length);

        BufferedReader reader = new BufferedReader(new FileReader(nodeDomainFile));
        int currentLineNumber = 0;
        for (int nodeId : sortedNodeIds) {
            // Yes, for some reason this is the best way.
            int linesToSkip = nodeId - currentLineNumber;
            for (int i = 0; i < linesToSkip; i++) reader.readLine();
            String domainName = reader.readLine().split("\t")[1];
            currentLineNumber = nodeId + 1;
            this.map.put(nodeId, domainName);
        }
        reader.close();
    }

    public String get(int nodeId) {
        return this.map.get(nodeId);
    }
}
