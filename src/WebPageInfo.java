import java.util.Comparator;

public class WebPageInfo implements Comparable, Comparator {
    String title = "";
    String link = "";
    int weight = 0;

    public WebPageInfo() {

    }

    public WebPageInfo(String title, String link) {
        this.title = title;
        this.link = link;
    }

    @Override
    public int compareTo(Object o) {
        int weightDiff = ((WebPageInfo)o).weight - this.weight;
        return weightDiff;
    }

    @Override
    public int compare(Object o1, Object o2) {
        return ((WebPageInfo)o1).compareTo((WebPageInfo)o2);
    }

    private boolean isSameTitle(WebPageInfo right) {
        return title.equals(right.title);
    }

    private boolean isSameLink(WebPageInfo right) {
        return link.equals(right.link);
    }

    public boolean equals(Object o) {
        if (o instanceof WebPageInfo) {
            WebPageInfo right = (WebPageInfo) o;
            return isSameTitle(right) && isSameLink(right);
        }
        return false;
    }

    public int hashCode() {
        return title.hashCode() + link.hashCode();
    }
}
