import java.util.*;

public class MotherSpider extends Thread {
    private final WebCrawlerGUI gui;
    private final Set<ChildSpider> childSpiders = new HashSet<>();
    private final Set<ChildSpider> sleepyChildSpiders = new HashSet<>();
    private boolean isAllChildWait = false;

    protected final Set<String> pagesVisited = new HashSet<>();
    protected final List<String> pagesToVisit = new LinkedList<>();

    protected final Set<PageInfo> usefulPages = new HashSet<>();


    public MotherSpider(WebCrawlerGUI gui) {
        this.gui = gui;
    }

    public int getMaxSearchPages() {
        return gui.getMaxSearchPages();
    }

    public String getSearchKeyword() { return gui.getSearchKeyword(); }

    private void clearVisitedPages() {
        pagesVisited.clear();
    }

    private void resetToVisitPages() {
        pagesToVisit.clear();
        pagesToVisit.addAll(
                new LinkedList<>() {{
                    add("https://zh.wikipedia.org/wiki/維基百科");
                    add("https://zh.wikipedia.org/wiki/Wiki");
                }}
        );
    }

    private void clearPages() {
        clearVisitedPages();
        resetToVisitPages();
        usefulPages.clear();
    }

    private boolean isChildSpidersLessThanThreadsLimit() {
        return childSpiders.size() < gui.getMaxSearchThreads();
    }

    private boolean isChildSpidersLessThanPagesLimit() {
        return childSpiders.size() < gui.getMaxSearchPages();
    }

    private void createChildSpiders() {
        isAllChildWait = false;
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
        sleepyChildSpiders.clear();
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

    public void addSearchResultTableRowData(Object[] objects) {
        gui.addRowDataToSearchResultTable(objects);
    }

    public void removeAllSearchResultTableRow() {
        gui.removeAllSearchResultTableRow();
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

        while(pagesVisited.contains(nextUrl)) {
            nextUrl = pagesToVisit.remove(0);
        }
        pagesVisited.add(nextUrl);

        return nextUrl;
    }

    public boolean isCommandContinueCrawl() {
        return pagesVisited.size() < getMaxSearchPages() && !isAllChildWait;
    }

    public void addWaitChild(ChildSpider cs) {
        sleepyChildSpiders.add(cs);
        if (sleepyChildSpiders.size() == childSpiders.size()) {
            isAllChildWait = true;
        }
        sleepyChildSpiders.notifyAll();
    }

    public void wakeUpChild() {
        for (ChildSpider cs:sleepyChildSpiders) {
            if (!pagesToVisit.isEmpty()) {
                cs.notify();
                sleepyChildSpiders.remove(cs);
            }
        }
    }

    public void addToUsefulPages(PageInfo page) {
        usefulPages.add(page);
    }

    public void addPagesToVisitPages(List<String> pages) {
        pagesToVisit.addAll(pages);
    }

    public void run() {

    }
}
