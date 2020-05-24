import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpiderLeg {
    private static final String USER_AGENT = "Chrome/81.0.4044.138";
    private String url;
    private final List<String> links = new LinkedList<>();
    private Document htmlDocument;
    private final String threadNumber;
    private String searchKeyword;

    public SpiderLeg(String threadNumber, String searchKeyword) {
        this.threadNumber = threadNumber;
        this.searchKeyword = searchKeyword;
    }

    public boolean isCrawl(String url) {
        this.url = url;
        try {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            this.htmlDocument = connection.get();

            //if (connection.response().statusCode() == 200) { }

            return connection.response().contentType().contains("text/html");
        } catch (Exception ex) {
            return false;
        }
    }

    private void handleNewLinks() {
        WebPageCommand wpc = WebPageCommand.NEWS;
        links.addAll(wpc.handleLink(htmlDocument));
    }

    private void handleYoutubeLinks() {
        WebPageCommand wpc = WebPageCommand.YOUTUBE;
        links.addAll(wpc.handleLink(htmlDocument));
    }

    private void handleShoppingLinks() {
        Elements linksOnPage = htmlDocument.select("a[href]");
        for (Element link : linksOnPage) {
            links.add(link.absUrl("href"));
        }
    }

    private void handleLinks(String searchClass) {
        if (searchClass.equals("新聞")) {
            handleNewLinks();
        } else if (searchClass.equals("Youtube")) {
            handleYoutubeLinks();
        } else if (searchClass.equals("購物")) {
            handleShoppingLinks();
        }
    }

    public List<String> getLinks(String searchClass) {
        handleLinks(searchClass);
        return links;
    }

    private int countKeyword() {
        return WebPageCommand.NEWS.getWeight(htmlDocument, searchKeyword);
    }

    public WebPageInfo getPageInfo() {
        WebPageInfo wpInfo = new WebPageInfo();
        wpInfo.link = url;

        Elements title = htmlDocument.select("title");
        if (title.size() >= 1) {
            String[] spt = title.first().toString().split("<title>|</title>");
            wpInfo.title = spt[1]; // spt[0] 為 "<title>" 前的字串，故要使用 spt[1] 來得到真正的 title
        }
        wpInfo.keywordCount = countKeyword();
        wpInfo.weight = wpInfo.keywordCount;
        //System.out.println(threadNumber + "title: " + wpInfo.title + " link: " + wpInfo.link);

        return wpInfo;
    }
}
