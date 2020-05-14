import java.util.Comparator;
import java.util.LinkedList;

public class PageInfo implements Comparable, Comparator {
    String link = null;
    String title = null;
    int weight = 0;
    int keywordCount = 0;

    public PageInfo() {

    }

    public PageInfo(String title, String link) {
        this.title = title;
        this.link = link;
    }

    @Override
    public int compareTo(Object o) {
        int weightDiff = this.weight - ((PageInfo)o).weight;
        int keywordCountDiff = this.keywordCount - ((PageInfo)o).keywordCount;
        return -(weightDiff + keywordCountDiff);
    }

    @Override
    public int compare(Object o1, Object o2) {
        return ((PageInfo)o1).compareTo((PageInfo)o2);
    }
}
