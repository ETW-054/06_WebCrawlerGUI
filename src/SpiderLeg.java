import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.List;

import static java.lang.Thread.sleep;

public class SpiderLeg {
    private static final String USER_AGENT = "Chrome/81.0.4044.138";
    private String url;
    private Document htmlDocument;
    private final WebPageCommand wpCommand;
    private final String searchKeyword;

    public SpiderLeg(WebPageCommand wpCommand, String searchKeyword) {
        this.wpCommand = wpCommand;
        this.searchKeyword = searchKeyword;
    }

    public boolean isCrawl(String url) {
        this.url = url;
        try {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            sleep(100); // 等待載入完成
            this.htmlDocument = connection.get();

            return connection.response().contentType().contains("text/html");
        } catch (Exception ex) {
            return false;
        }
    }

    public List<String> getLinks() {
        return wpCommand.handleLink(htmlDocument);
    }

    public WebPageInfo getWebPageInfo() {
        WebPageInfo wpInfo = new WebPageInfo();
        wpInfo.link = url;

        Elements title = htmlDocument.select("title");
        if (title.size() >= 1) {
            String[] spt = title.first().toString().split("<title>|</title>");
            try {
                wpInfo.title = spt[1]; // spt[0] 為 "<title>" 前的字串，故要使用 spt[1] 來得到真正的 title
            } catch (Exception e) {
                wpInfo.title = "";
            }
        }
        wpInfo.weight = wpCommand.getWeight(htmlDocument, searchKeyword);

        return wpInfo;
    }
}
