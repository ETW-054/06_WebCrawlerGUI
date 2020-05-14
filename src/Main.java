import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Main {
    Main() { }

    public void spiderMan1() throws IOException {
        List<String> links = new LinkedList<String>();
        Document doc = Jsoup.connect("https://zh.wikipedia.org/wiki/Wiki").get();

        Elements media = doc.select("[src]");
        Elements linksOnPage = doc.select("a[href]");

        System.out.println("Media: (" + media.size() + ")");
        System.out.println("Links: (" + linksOnPage.size() + ")");

        for (Element link:linksOnPage) {
            links.add(link.absUrl("href"));
        }
    }

    public void spiderMan2() {
        //new ChildSpider().search("https://zh.wikipedia.org/wiki/Wiki", "wiki");
        //new ChildSpider().anotherSearch("http://localhost/time_axis/main.html", "YA");
    }

    public static void main(String[] args) {
        WebCrawlerGUI dialog = new WebCrawlerGUI();
        dialog.execute();

        //new Main().spiderMan2();
    }
}
