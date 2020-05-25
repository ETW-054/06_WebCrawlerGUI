import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum WebPageCommand implements Commandable {
    // 新聞的權重使用 時間、標題、網頁內容 來記算
    NEWS {
        public List<String> getDefaultUrl(String keyword) {
            return new LinkedList<>() {{
                add("https://www.google.com/search?q=" + keyword + "&safe=strict&tbm=nws&start=0");
                add("https://www.google.com/search?q=" + keyword + "&safe=strict&tbm=nws&start=10");
                add("https://www.google.com/search?q=" + keyword + "&safe=strict&tbm=nws&start=20");
            }};
        }

        public boolean isDefaultUrl(String url) {
            return isDefaultUrl__(url);
        }

        public List<String> handleLink(Document htmlDocument) {
            String regex = "(https://tw.news.yahoo.com/|https://udn.com/news/|https://udn.com/|" +
                    "https://www.chinatimes.com/|https://news.ltn.com.tw/|https://talk.ltn.com.tw/|" +
                    "https://www.ettoday.net/|https://www.setn.com/|https://www.cna.com.tw/|https://www.epochtimes.com/|" +
                    "https://tw.appledaily.com/|https://money.udn.com/|https://www.ttv.com.tw/|" +
                    "https://newtalk.tw/|https://www.thenewslens.com/|https://www.storm.mg/)([^&]|[0-9A-Za-z~!@#$%^*()\\-+=])+";
            return handleLink__(htmlDocument, regex);
        }

        public WebPageInfo getWebPageInfo(Document htmlDocument, String url, String keyword) {
            WebPageInfo wpInfo = getWebPageInfo__(htmlDocument, url);
            String text = htmlDocument.toString().toLowerCase();
            wpInfo.weight = countKeyword__(text, keyword) + getTitleFactor__(wpInfo.title, keyword, 150) +
                    getTimeFactor(text);
            return wpInfo;
        }

        private long getTimeDiff(String publishDate) {
            try {
                SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String current = sdFormat.format(new Date());
                long hTime = sdFormat.parse(publishDate).getTime();
                long cTime = sdFormat.parse(current).getTime();
                return (cTime - hTime) / 1000; // ms to s
            } catch (Exception e) {
                return -1; // 抓不到時間
            }
        }

        private double calculateTimeFactor_(double num) {
            return 200 * Math.pow(0.75, num);
        }

        private double calculateTimeFactor(long timeDiff) {
            if (timeDiff == -1) { // 沒有時間
              return 0;
            } else if (timeDiff <= 900) { // 1刻 -> 最新新聞
                return calculateTimeFactor_(0);
            } else if (timeDiff <= 3600) { // 1時 -> 非常新的新聞
                return calculateTimeFactor_(1);
            } else if (timeDiff <= 86400) { // 1天 -> 很新的新聞
                return calculateTimeFactor_(2);
            } else if (timeDiff <= 604800) { // 1周 -> 不新也不舊的新聞
                return calculateTimeFactor_(3);
            } else if (timeDiff <= 2592000) { // 1月 -> 有點時間的新聞
                return calculateTimeFactor_(4);
            } else if (timeDiff <= 7776000) { // 1季 -> 有點久的新聞
                return calculateTimeFactor_(5);
            } else if (timeDiff <= 31104000) { // 1年 -> 很久的新聞
                return calculateTimeFactor_(6);
            } else { // 大於1年 -> 超久的新聞
                return calculateTimeFactor_(7);
            }
        }

        private double getTimeFactor(String text) {
            if (text == null) { return 0; }
            Pattern pattern = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                long timeDiff = getTimeDiff(matcher.group());
                return calculateTimeFactor(timeDiff);
            } else {
                return 0;
            }
        }
    },

    // Youtube的權重使用 觀看次數、評價、時間、標題、網頁內容 來記算
    YOUTUBE {
        public List<String> getDefaultUrl(String keyword) {
            return new LinkedList<>() {{
                add("https://www.youtube.com/results?search_query=" + keyword);
            }};
        }

        public boolean isDefaultUrl(String url) {
            return isDefaultUrl__(url);
        }

        public List<String> handleLink(Document htmlDocument) {
            String regex = "https://www.youtube.com/watch\\?.*";
            return handleLink__(htmlDocument, regex);
        }

        public WebPageInfo getWebPageInfo(Document htmlDocument, String url, String keyword) {
            WebPageInfo wpInfo = getWebPageInfo__(htmlDocument, url);
            String text = htmlDocument.toString().toLowerCase();

            wpInfo.weight = (countKeyword__(htmlDocument.toString().toLowerCase(), keyword) * 0.8 +
                    getTitleFactor__(wpInfo.title, keyword, 20)) * getTimeFactor(text) * getViewFactor(text);
            return wpInfo;
        }

        private long getTimeDiff(String publishDate) {
            try {
                SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
                String current = sdFormat.format(new Date());
                long hTime = sdFormat.parse(publishDate).getTime();
                long cTime = sdFormat.parse(current).getTime();
                return (cTime - hTime) / 1000; // ms to s
            } catch (Exception e) {
                return -1; // 抓不到時間
            }
        }

        private double calculateTimeFactor_(double num) {
            return Math.pow(0.9, num);
        }

        private double calculateTimeFactor(long timeDiff) {
            if (timeDiff == -1) { // 沒有時間
                return 0.55;
            } else if (timeDiff <= 86400) { // 1天 -> 最新的影片
                return calculateTimeFactor_(0);
            } else if (timeDiff <= 604800) { // 1周 -> 很新的影片
                return calculateTimeFactor_(1);
            } else if (timeDiff <= 2592000) { // 1月 -> 有點時間的影片
                return calculateTimeFactor_(2);
            } else if (timeDiff <= 7776000) { // 1季 -> 有點久的影片
                return calculateTimeFactor_(3);
            } else if (timeDiff <= 31104000) { // 1年 -> 很久的影片
                return calculateTimeFactor_(4);
            } else { // 大於1年 -> 超久的影片
                return calculateTimeFactor_(5);
            }
        }

        private double getTimeFactor(String text) {
            if (text == null) { return 0; }
            Pattern pattern = Pattern.compile("\\d\\d\\d\\d年\\d?\\d月\\d\\d日");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String tt = matcher.group();
                long timeDiff = getTimeDiff(tt);
                return calculateTimeFactor(timeDiff);
            } else {
                return 0.55;
            }
        }

        private double getViewCount(String text) {
            Pattern pattern = Pattern.compile("<div class=\"watch-view-count\">\\s*觀看次數：(\\d+,?)+次\\s*</div>");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) { // 只有第一筆是標題的觀看次數，後面幾筆的是其它的
                return getNumber__(matcher.group());
            }
            return 0;
        }

        private double getLikeDislikeFactor(String text) {
            Pattern likePattern = Pattern.compile("(\\d+,?)+ 人喜歡");
            Matcher likeMatcher = likePattern.matcher(text);
            double likeCount = 0;
            Pattern dislikePattern = Pattern.compile("(\\d+,?)+ 人不喜歡");
            Matcher dislikeMatcher = dislikePattern.matcher(text);
            double dislikeCount = 0;

            if (likeMatcher.find()) { // 只有第一筆是標題的觀看次數，後面幾筆的是其它的
                likeCount = getNumber__(likeMatcher.group());
            }
            if (dislikeMatcher.find()) {
                dislikeCount = getNumber__(dislikeMatcher.group());
            }

            if (likeCount + dislikeCount == 0) {
                return 0.5;
            } else {
                return likeCount / (likeCount + dislikeCount);
            }
        }

        private double getViewFactor(String text) {
            if (text == null) { return 0; }
            return Math.log(getViewCount(text) * getLikeDislikeFactor(text));
        }
    },

    // 購物的權重使用 價格、標題、網頁內容 來記算
    SHOPPING {
        public List<String> getDefaultUrl(String keyword) {
            return new LinkedList<>() {{
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "&sort=rel");
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "&pg=2&sort=rel");
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "&sort=p13n");
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "&pg=2&sort=p13n");
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "&sort=-sales");
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "pg=2&sort=-sales");
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "&sort=-ptime");
                add("https://tw.buy.yahoo.com/search/product?p=" + keyword + "&pg=2&sort=-ptime");
            }};
        }

        public boolean isDefaultUrl(String url) {
            return isDefaultUrl__(url);
        }

        public List<String> handleLink(Document htmlDocument) {
            String regex = "https://tw.buy.yahoo.com/gdsale/(.)*\\.html";
            return handleLink__(htmlDocument, regex);
        }

        public WebPageInfo getWebPageInfo(Document htmlDocument, String url, String keyword) {
            WebPageInfo wpInfo = getWebPageInfo__(htmlDocument, url);
            String text = htmlDocument.toString().toLowerCase();
            wpInfo.weight = countKeyword__(text, keyword) * 0.8 + getTitleFactor__(wpInfo.title, keyword, 120) -
                    getPriceFactor(text);
            System.out.println(wpInfo.toString());
            return wpInfo;
        }

        private double getPriceFactor(String text) {
            if (text == null) { return 0; }
            Pattern pattern = Pattern.compile("\"price\":\"(\\d+,?)+\"");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                // 使用換底公式求 log1.2(price) = log2(price) / log2(1.2)
                return Math.log(getNumber__(matcher.group())) / Math.log(1.2);
            } else {
                return 100;
            }
        }
    },

    // 書藉的權重使用 價格、標題、網頁內容 來記算
    BOOK {
        public List<String> getDefaultUrl(String keyword) {
            return new LinkedList<>() {{
                add("https://search.books.com.tw/search/query/cat/all/key/" + keyword + "/sort/1/page/1/v/0/");
                add("https://search.books.com.tw/search/query/cat/all/key/" + keyword + "/sort/1/page/2/v/0/");
                add("https://search.books.com.tw/search/query/cat/all/key/" + keyword + "/sort/1/page/3/v/0/");
            }};
        }

        public boolean isDefaultUrl(String url) {
            return isDefaultUrl__(url);
        }

        public List<String> handleLink(Document htmlDocument) {
            List<String> tempLinks = new LinkedList<>();
            Elements linksOnPage = htmlDocument.select("a[href]");
            Pattern linkPattern = Pattern.compile("search.books.com.tw/redirect/move/key/.*/area/mid/item/.*");
            Matcher linkMatcher;

            // 將符合上述regular expression的網址加入 to visit list
            for (Element link : linksOnPage) {
                linkMatcher = linkPattern.matcher(link.absUrl("href"));
                while (linkMatcher.find()) {
                    tempLinks.add(linkMatcher.group());
                }
            }

            List<String> links = new LinkedList<>();
            Pattern bookPattern = Pattern.compile("\\d{10}|E\\D{9}|CN\\d{8}");
            Matcher bookMatcher;

            // 將符合上述regular expression的網址加入 to visit list
            for (String link : tempLinks) {
                bookMatcher = bookPattern.matcher(link);
                while (bookMatcher.find()) {
                    links.add("https://www.books.com.tw/products/" + bookMatcher.group() + "?sloc=main");
                }
            }
            return links;
        }

        public WebPageInfo getWebPageInfo(Document htmlDocument, String url, String keyword) {
            WebPageInfo wpInfo = getWebPageInfo__(htmlDocument, url);
            String text = htmlDocument.toString().toLowerCase();
            wpInfo.weight = countKeyword__(text, keyword) * 0.8 + getTitleFactor__(wpInfo.title, keyword, 120) -
                    getPriceFactor(text);
            return wpInfo;
        }

        private double getPriceFactor(String text) {
            if (text == null) { return 0; }
            Pattern pattern = Pattern.compile("\"price\":\\d*");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                // 使用換底公式求 log1.2(price) = log2(price) / log2(1.2)
                return Math.log(getNumber__(matcher.group())) / Math.log(1.2);
            } else {
                return 100;
            }
        }
    };

    private static List<String> handleLink__(Document htmlDocument, String regex) {
        List<String> urls = new LinkedList<>();
        Elements linksOnPage = htmlDocument.select("a[href]");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher;

        // 將符合上述regular expression的網址加入 to visit list
        for (Element link : linksOnPage) {
            matcher = pattern.matcher(link.absUrl("href"));
            while (matcher.find()) {
                urls.add(matcher.group());
            }
        }
        return urls;
    }

    private static boolean isDefaultUrl__(String link) {
        Pattern pattern = Pattern.compile("(https://www.google.com/search\\?q=|" +
                "https://www.youtube.com/results\\?search_query=|" +
                "https://tw.buy.yahoo.com/search/product\\?p=|" +
                "https://search.books.com.tw/search/query/).*");
        Matcher matcher = pattern.matcher(link);

        return matcher.find();
    }

    private static int countKeyword__(String text, String keyword) {
        int count = 0;
        String[] spt = keyword.split("\\s+"); // 關鍵字有可能是以空白分開，因此這邊要將其拆開搜尋
        for (String s:spt) {
            Pattern pattern = Pattern.compile(s.toLowerCase());
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }

    private static WebPageInfo getWebPageInfo__(Document htmlDocument, String url) {
        WebPageInfo wpInfo = new WebPageInfo();
        wpInfo.url = url;
        wpInfo.title = htmlDocument.title();

        return wpInfo;
    }

    private static double getTitleFactor__(String title, String keyword) {
        return getTitleFactor__(title, keyword, 50);
    }

    private static double getTitleFactor__(String title, String keyword, double reward) {
        return title.contains(keyword) ? reward : 0;
    }

    private static double getNumber__(String str) {
        Pattern pattern = Pattern.compile("(\\d+,?)+");
        Matcher matcher = pattern.matcher(str.toLowerCase());
        if (matcher.find()) {
            String oldStr = ",";
            String newStr = "";
            return Double.parseDouble(matcher.group().replace(oldStr, newStr));
        }
        return 0;
    }
}
