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
    private String keyword;

    public SpiderLeg(int spiderNumber) {
        this.spiderNumber = spiderNumber;
    }

    public boolean isCrawl(String url) {
        this.url = url;
        try {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            this.htmlDocument = htmlDocument;
            //System.out.println(htmlDocument);

            if (connection.response().statusCode() == 200) {
                System.out.println("\nChild " + spiderNumber + " **Visiting** Received web page at " + url);
            }

            if (!connection.response().contentType().contains("text/html")) {
                System.out.println("Child " + spiderNumber + " **Failure** Retrieved something other than HTML");
                return false;
            }

            Elements linksOnPage = htmlDocument.select("a[href]");
            for (Element link : linksOnPage) {
                links.add(link.absUrl("href"));
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public List<String> getLinks() {
        return links;
    }

    public boolean searchForWord(String searchWord) {
        if (htmlDocument == null) {
            System.out.println("Child " + spiderNumber + " Error !! Empty page");
            return false;
        }
        this.keyword = searchWord;
        System.out.println("Child " + spiderNumber + " Searching for the word '" + searchWord + "'...");

        String bodyText = htmlDocument.body().text();
        return bodyText.toLowerCase().contains((searchWord.toLowerCase()));
    }

    private int countKeyword() {
        Pattern pattern = Pattern.compile(keyword);
        Matcher matcher = pattern.matcher(htmlDocument.toString());
        int count = 0;
        while (matcher.find()) {
            count++;
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
