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
    private final int spiderNumber;
    private String searchKeyword;

    public SpiderLeg(int spiderNumber) {
        this.spiderNumber = spiderNumber;
    }

    public boolean isCrawl(String url) {
        this.url = url;
        try {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            this.htmlDocument = connection.get();

            if (connection.response().statusCode() == 200) {
                System.out.println("\nChild " + spiderNumber + " **Visiting** Received web page at " + url);
            }

            if (!connection.response().contentType().contains("text/html")) {
                System.out.println("Child " + spiderNumber + " **Failure** Retrieved something other than HTML");
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void handleNewLinks() {
        Elements linksOnPage = htmlDocument.select("a[href]");
        Pattern pattern = Pattern.compile("(https://tw.news.yahoo.com/|https://udn.com/news/|https://udn.com/|" +
                "https://www.chinatimes.com/|https://news.ltn.com.tw/|https://talk.ltn.com.tw/|" +
                "https://www.ettoday.net/|https://www.setn.com/|https://www.cna.com.tw/|https://www.epochtimes.com/|" +
                "https://tw.appledaily.com/|https://money.udn.com/|https://www.ttv.com.tw/|" +
                "https://newtalk.tw/|https://www.thenewslens.com/|https://www.storm.mg/)([^&]|[0-9A-Za-z~!@#$%^*()\\-+=])*");
        Matcher matcher;

        for (Element link : linksOnPage) {
            matcher = pattern.matcher(link.absUrl("href"));
            while (matcher.find()) {
                links.add(matcher.group());
            }
        }
    }

    private void handleYoutubeLinks() {
        Elements linksOnPage = htmlDocument.select("a[href]");
        Pattern pattern = Pattern.compile("https://www.youtube.com/watch\\?.*");
        Matcher matcher;

        for (Element link : linksOnPage) {
            matcher = pattern.matcher(link.absUrl("href"));
            while (matcher.find()) {
                links.add(matcher.group());
            }
        }
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

    public boolean searchForWord(String searchKeyword) {
        if (htmlDocument == null) {
            System.out.println("Child " + spiderNumber + " Error !! Empty page");
            return false;
        }
        this.searchKeyword = searchKeyword;
        System.out.println("Child " + spiderNumber + " Searching for the word '" + searchKeyword + "'...");

        String text = htmlDocument.toString().toLowerCase();
        String[] spt = searchKeyword.split("(\\s)*");
        for (String s:spt) {
            if (text.contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private int countKeyword() {
        int count = 0;
        String[] spt = searchKeyword.split("(\\s)*");
        for (String s:spt) {
            Pattern pattern = Pattern.compile(s.toLowerCase());
            Matcher matcher = pattern.matcher(htmlDocument.toString().toLowerCase());
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }

    public PageInfo getPageInfo() {
        PageInfo pageInfo = new PageInfo();
        pageInfo.link = url;

        Elements title = htmlDocument.select("title");
        if (!title.isEmpty()) {
            String[] spt = title.first().toString().split("<title>|</title>");
            pageInfo.title = spt[1];
        }
        pageInfo.keywordCount = countKeyword();

        return pageInfo;
    }
}
