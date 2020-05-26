import java.util.Comparator;

public class WebPageInfo implements Comparable, Comparator {
    String title = "";
    String url = "";
    double weight = 0;
    String info = "";

    public WebPageInfo() { }

    @Override
    public int compareTo(Object o) {
        try {
            return (int) (((WebPageInfo) o).weight - this.weight);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int compare(Object o1, Object o2) { return ((WebPageInfo)o1).compareTo(o2); }

    public String toString() { return "title: " + title + " link: " + url + " weight: " + weight; }

    private boolean isSameTitle(WebPageInfo right) { return title.equals(right.title); }

    private boolean isSameLink(WebPageInfo right) { return url.equals(right.url); }

    public boolean equals(Object o) {
        if (o instanceof WebPageInfo) {
            WebPageInfo right = (WebPageInfo) o;
            return isSameTitle(right) && isSameLink(right);
        }
        return false;
    }

    public int hashCode() { return title.hashCode() + url.hashCode(); }
}
