import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MotherSpider extends Thread {
    public static class SearchHistory {
        String searchClass;
        String searchKeyword;
        int maxSearchPages;

        SearchHistory(String searchClass, String searchKeyword, int maxSearchPages) {
            this.searchClass = searchClass;
            this.searchKeyword = searchKeyword;
            this.maxSearchPages = maxSearchPages;
        }

        public String toString() {
            return "class: " + searchClass + " keyword: " + searchKeyword + " max pages: " + maxSearchPages;
        }

        private boolean isSameClass(SearchHistory right) {
            return searchClass.equals(right.searchClass);
        }

        private boolean isSameKeyword(SearchHistory right) {
            return searchKeyword.equals(right.searchKeyword);
        }

        private boolean isSameMaxPages(SearchHistory right) {
            return maxSearchPages == right.maxSearchPages;
        }

        public boolean equals(Object o) {
            if (o instanceof SearchHistory) {
                SearchHistory right = (SearchHistory) o;
                return isSameClass(right) && isSameKeyword(right) && isSameMaxPages(right);
            }
            return false;
        }

        public int hashCode() {
            return searchClass.hashCode() + searchKeyword.hashCode() + maxSearchPages;
        }
    }

    public static class SearchResultHistory {
        Date date;
        PageInfo[] pagesInfo;

        SearchResultHistory(Date date, PageInfo[] pagesInfo) {
            this.date = date;
            this.pagesInfo = pagesInfo;
        }
    }

    private final WebCrawlerGUI gui;

    protected ConcurrentHashMap<SearchHistory, SearchResultHistory> searchHistory = new ConcurrentHashMap<>();

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

    private Set<PageInfo> searchPages() {
        ChildSpidersCommander commander = new ChildSpidersCommander(
                getSearchKeyword(), getSearchClass(), getMaxSearchPages(), getMaxSearchThreads());

        commander.execute();

        return commander.getUsefulPages();
    }

    private PageInfo[] findPages() {
        SearchHistory sh = new SearchHistory(getSearchClass(), getSearchKeyword(), getMaxSearchPages());
        System.out.println(sh.toString());
        if (searchHistory.containsKey(sh)) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> contain");
            return searchHistory.get(sh).pagesInfo;
        }

        Set<PageInfo> searchResult = searchPages();
        PageInfo[] arr = new PageInfo[searchResult.size()];
        searchResult.toArray(arr);
        Arrays.sort(arr);

        SearchResultHistory srh = new SearchResultHistory(new Date(), arr);
        searchHistory.put(sh, srh);
        System.out.println("new " + sh.toString());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> PPPPPPPPP");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + searchHistory.size());
        return arr;
    }

    private void showSearchResult(PageInfo[] arr) {
        int count = 1;
        for (PageInfo page:arr) {
            Object[] objects = { count++, page.title, page.link, page.weight + page.keywordCount };
            addSearchResultTableRowData(objects);
        }
    }

    public void assignChildSpiders() {
        removeAllSearchResultTableRow();
        showSearchResult(findPages());
        gui.showSearchComplete();
    }

    public void run() {

    }
}
