import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

public class NodeDomainMap {
    private Hashtable<Integer, String> map;
    
    NodeDomainMap(Iterable<Integer> nodeIds, String nodeDomainFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(nodeDomainFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // process the line.
            }
            reader.close();
        }
        
        for (int nodeId : nodeIds) {

        }
    }
}
