import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum WebPageCommand implements Commandable {
    NEWS {
        public List<String> getDefaultUrl(String keyword) {
            return new LinkedList<>() {{
                add("https://www.google.com/search?q="+ keyword + "&safe=strict&tbm=nws&start=0");
                add("https://www.google.com/search?q="+ keyword + "&safe=strict&tbm=nws&start=10");
                add("https://www.google.com/search?q="+ keyword + "&safe=strict&tbm=nws&start=20");
            }};
        }

        public boolean isDefaultUrl(String link) {
            return isDefaultUrl_(link);
        }

        public List<String> handleLink(Document htmlDocument) {
            String regex = "(https://tw.news.yahoo.com/|https://udn.com/news/|https://udn.com/|" +
                    "https://www.chinatimes.com/|https://news.ltn.com.tw/|https://talk.ltn.com.tw/|" +
                    "https://www.ettoday.net/|https://www.setn.com/|https://www.cna.com.tw/|https://www.epochtimes.com/|" +
                    "https://tw.appledaily.com/|https://money.udn.com/|https://www.ttv.com.tw/|" +
                    "https://newtalk.tw/|https://www.thenewslens.com/|https://www.storm.mg/)([^&]|[0-9A-Za-z~!@#$%^*()\\-+=])*";
            return handleLink_(htmlDocument, regex);
        }

        public int getWeight(Document htmlDocument, String keyword) {
            return countKeyword_(htmlDocument, keyword);
        }
    },

    YOUTUBE {
        public List<String> getDefaultUrl(String keyword) {
            return new LinkedList<>() {{
                add("https://www.youtube.com/results?search_query="+ keyword);
            }};
        }

        public boolean isDefaultUrl(String link) {
            return isDefaultUrl_(link);
        }

        public List<String> handleLink(Document htmlDocument) {
            String regex = "https://www.youtube.com/watch\\?.*";
            return handleLink_(htmlDocument, regex);
        }

        public int getWeight(Document htmlDocument, String keyword) {
            return countKeyword_(htmlDocument, keyword);
        }
    };

    public List<String> handleLink_(Document htmlDocument, String regex) {
        List<String> links = new LinkedList<>();
        Elements linksOnPage = htmlDocument.select("a[href]");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher;

        // 將符合上述regular expression的網址加入 to visit list
        for (Element link : linksOnPage) {
            matcher = pattern.matcher(link.absUrl("href"));
            while (matcher.find()) {
                links.add(matcher.group());
            }
        }
        return links;
    }

    public boolean isDefaultUrl_(String link) {
        Pattern pattern = Pattern.compile("(https://www.google.com/search\\?q=|" +
                "https://www.youtube.com/results\\?search_query=).*");
        Matcher matcher = pattern.matcher(link);

        return matcher.find();
    }

    public int countKeyword_(Document htmlDocument, String keyword) {
        int count = 0;
        String[] spt = keyword.split("\\s+"); // 關鍵字有可能是以空白分開，因此這邊要將其拆開搜尋
        for (String s:spt) {
            Pattern pattern = Pattern.compile(s.toLowerCase());
            Matcher matcher = pattern.matcher(htmlDocument.toString().toLowerCase());
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }
}
