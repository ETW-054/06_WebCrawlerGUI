import org.jsoup.nodes.Document;
import java.util.List;

// 為 search class 設計的指令介面
public interface Commandable {
    List<String> getDefaultUrl(String keyword);
    boolean isDefaultUrl(String url);
    List<String> handleLink(Document htmlDocument);
    WebPageInfo getWebPageInfo(Document htmlDocument, String url, String keyword);
}
