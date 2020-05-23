import org.jsoup.nodes.Document;
import java.util.List;

public interface Commandable {
    List<String> getDefaultUrl(String keyword);
    boolean isDefaultUrl(String link);
    List<String> handleLink(Document htmlDocument);
    int getWeight(Document htmlDocument, String keyword);
}
