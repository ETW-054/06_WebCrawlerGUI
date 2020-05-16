import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MotherSpider extends Thread {
    private final WebCrawlerGUI gui;

    protected final Set<PageInfo> usefulPages = new HashSet<>();

    /// Constructor
    public MotherSpider(WebCrawlerGUI gui) {
        this.gui = gui;
    }

    // From GUI
    public String getSearchKeyword() { return gui.getSearchKeyword(); }

    // From GUI
    public String getSearchClass() { return gui.getSearchClass(); }

    // From GUI
    public int getMaxSearchPages() { return gui.getMaxSearchPages(); }

    // From GUI
    public int getMaxSearchThreads() { return gui.getMaxSearchThreads(); }

    // To GUI
    public void addSearchResultTableRowData(Object[] objects) {
        gui.addRowDataToSearchResultTable(objects);
    }

    // To GUI
    public void removeAllSearchResultTableRow() {
        gui.removeAllSearchResultTableRow();
    }

    private Set<PageInfo> findPages() {
        ChildSpidersCommander commander = new ChildSpidersCommander(
                getSearchKeyword(), getSearchClass(), getMaxSearchPages(), getMaxSearchThreads());

        commander.execute();

        return commander.getUsefulPages();
    }

    private void showSearchResult() {
        PageInfo[] arr = new PageInfo[usefulPages.size()];
        usefulPages.toArray(arr);
        Arrays.sort(arr);
        int count = 1;
        for (PageInfo page:arr) {
            Object[] objects = { count++, page.title, page.link, page.weight + page.keywordCount };
            addSearchResultTableRowData(objects);
        }
    }

    private void showSearchResult(Set<PageInfo> pages) {
        PageInfo[] arr = new PageInfo[pages.size()];
        pages.toArray(arr);
        Arrays.sort(arr);
        int count = 1;
        for (PageInfo page:arr) {
            Object[] objects = { count++, page.title, page.link, page.weight + page.keywordCount };
            addSearchResultTableRowData(objects);
        }
    }

    public void assignChildSpiders() {
        removeAllSearchResultTableRow();
        Set<PageInfo> searchResult = findPages();
        showSearchResult(searchResult);
        gui.showSearchComplete();
    }

    public void run() {

    }
}
