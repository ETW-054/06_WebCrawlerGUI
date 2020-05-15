import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MotherSpider extends Thread {
    private final WebCrawlerGUI gui;
    private final Set<ChildSpider> childSpiders = new HashSet<>();
    private final Lock lock = new ReentrantLock();

    protected final Set<String> pagesVisited = new HashSet<>();
    protected final List<String> pagesToVisit = new LinkedList<>();

    protected final Set<PageInfo> usefulPages = new HashSet<>();

    /// Constructor
    public MotherSpider(WebCrawlerGUI gui) {
        this.gui = gui;
    }

    // From GUI
    public int getMaxSearchPages() {
        return gui.getMaxSearchPages();
    }

    // From GUI
    public String getSearchKeyword() { return gui.getSearchKeyword(); }

    private void clearVisitedPages() {
        pagesVisited.clear();
    }

    private List<String> getNewsUrl() {
        return new LinkedList<>() {{
            add("https://www.google.com.tw/search?q="+ getSearchKeyword() + "&safe=strict&tbm=nws&start=0");
            add("https://www.google.com.tw/search?q="+ getSearchKeyword() + "&safe=strict&tbm=nws&start=10");
            add("https://www.google.com.tw/search?q="+ getSearchKeyword() + "&safe=strict&tbm=nws&start=20");
        }};
    }

    private List<String> getYoutubeUrl() {

        return null;
    }

    private List<String> getShoppingUrl() {

        return null;
    }

    private void resetToVisitPages() {
        pagesToVisit.clear();
        pagesToVisit.addAll(getNewsUrl());
    }

    private void clearPages() {
        clearVisitedPages();
        resetToVisitPages();
        usefulPages.clear();
    }

    // From GUI
    private boolean isChildSpidersLessThanThreadsLimit() {
        return childSpiders.size() < gui.getMaxSearchThreads();
    }

    // From GUI
    private boolean isChildSpidersLessThanPagesLimit() {
        return childSpiders.size() < gui.getMaxSearchPages();
    }

    private void createChildSpiders() {
        int childCount = 0;
        while (isChildSpidersLessThanThreadsLimit() && isChildSpidersLessThanPagesLimit()) {
            childSpiders.add(new ChildSpider(this, childCount++));
        }
    }

    private void commandChildSpiderStartCrawl() {
        for (ChildSpider cs : childSpiders) {
            cs.start();
        }
    }

    private void waitForChildSpider() {
        try {
            for (ChildSpider cs : childSpiders) {
                cs.join();
            }
        } catch (Exception ignored) { }
    }

    private void removeChildSpider() {
        childSpiders.clear();
    }

    // To GUI
    public void addSearchResultTableRowData(Object[] objects) {
        gui.addRowDataToSearchResultTable(objects);
    }

    // To GUI
    public void removeAllSearchResultTableRow() {
        gui.removeAllSearchResultTableRow();
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

    public void assignChildSpiders() {
        removeAllSearchResultTableRow();
        clearPages();
        createChildSpiders();
        commandChildSpiderStartCrawl();
        waitForChildSpider();
        removeChildSpider();
        showSearchResult();
        gui.showSearchComplete();
    }

    public String getNextUrl() {
        String nextUrl = pagesToVisit.remove(0);

        while (pagesVisited.contains(nextUrl)) {
            nextUrl = pagesToVisit.remove(0);
        }
        pagesVisited.add(nextUrl);

        return nextUrl;
    }

    public boolean hasNotReachMaxSearchPages() {
        return pagesVisited.size() < getMaxSearchPages();
    }

    private boolean isSamePage(PageInfo left, PageInfo right) {
        return left.title.equals(right.title) || left.link.equals(right.link);
    }

    public void addToUsefulPages(PageInfo newPage) {
        for (PageInfo page:usefulPages) {
            if (isSamePage(page, newPage)) {
                return;
            }
        }
        usefulPages.add(newPage);
    }

    public void addPagesToVisitPages(List<String> pages) {
        pagesToVisit.addAll(pages);
    }

    // From GUI
    public String getSearchClass() {
        return gui.getSearchClass();
    }

    public void run() {

    }
}
