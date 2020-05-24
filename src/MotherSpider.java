import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.min;

public class MotherSpider {
    private final WebCrawlerGUI gui;
    protected ConcurrentHashMap<SearchSettingHistory, SearchResultHistory> searchHistory = new ConcurrentHashMap<>();
    private WebPageInfo[] searchResult;
    private int currentPageCount;
    private int maxPageCount;
    private int pageLimit;

    /// Constructor
    public MotherSpider(WebCrawlerGUI gui) {
        this.gui = gui;
    }

    // From GUI
    public String getSearchKeyword() { return gui.getSearchKeyword(); }

    // From GUI
    public String getSearchClass() { return gui.getSearchClass(); }

    // From GUI
    public int getMaxSearchLimit() { return gui.getMaxSearchLimit(); }

    // From GUI
    public int getMaxSearchThreads() { return gui.getMaxSearchThreads(); }

    // To GUI
    public void addSearchResultTableRow(Object[] objects) {
        gui.addSearchResultTableRow(objects);
    }

    // To GUI
    public void removeAllSearchResultTableRow() {
        gui.removeAllSearchResultTableRow();
    }

    private WebPageInfo[] searchWebPages() {
        ChildSpidersCommander commander = new ChildSpidersCommander(
                getSearchKeyword(), getSearchClass(), getMaxSearchLimit(), getMaxSearchThreads());

        commander.start();
        try {
            commander.join();
        } catch (Exception ignore) { }

        Set<WebPageInfo> resultTemp = commander.getUsefulPages();
        WebPageInfo[] searchResultTemp = new WebPageInfo[resultTemp.size()];
        resultTemp.toArray(searchResultTemp);
        Arrays.sort(searchResultTemp);

        return searchResultTemp;
    }

    private void findPages() {
        SearchSettingHistory ssh = new SearchSettingHistory(getSearchClass(), getSearchKeyword(), getMaxSearchLimit());

        if (searchHistory.containsKey(ssh)) {
            searchResult =  searchHistory.get(ssh).wpsInfo;
            return;
        }

        searchResult = searchWebPages();
        SearchResultHistory srh = new SearchResultHistory(new Date(), searchResult);
        searchHistory.put(ssh, srh);
    }

    private void setTotalSearchedPagesNumberLabel(String text) {
        gui.setTotalSearchedWebPagesLabel(text);
    }

    private void setSearchResultTable(int pageCount) {
        currentPageCount = pageCount;
        int maxLimit = min((currentPageCount + 1) * pageLimit, searchResult.length);

        for (int i = currentPageCount * pageLimit; i < maxLimit; i++) {
            Object[] objects = { i + 1, searchResult[i].title, searchResult[i].link, searchResult[i].weight };
            addSearchResultTableRow(objects);
        }
    }

    private void setCurrentPageNumberLabelZero() {
        gui.setCurrentPageNumberLabel("0 / 0");
    }

    private void setCurrentPageNumberLabel(int pageCount) {
        gui.setCurrentPageNumberLabel(pageCount + " / " + (maxPageCount + 1));
    }

    public void toFirstPage() {
        if (searchResult.length == 0) { return; }
        removeAllSearchResultTableRow();
        setSearchResultTable(0);
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void toFrontPage() {
        if (searchResult.length == 0) { return; }
        removeAllSearchResultTableRow();
        if (currentPageCount == 0) {
            setSearchResultTable(0);
        } else {
            setSearchResultTable(--currentPageCount);
        }
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void toNextPage() {
        if (searchResult.length == 0) { return; }
        removeAllSearchResultTableRow();
        if (currentPageCount == maxPageCount) {
            setSearchResultTable(maxPageCount);
        } else {
            setSearchResultTable(++currentPageCount);
        }
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void toLastPage() {
        if (searchResult.length == 0) { return; }
        removeAllSearchResultTableRow();
        setSearchResultTable(maxPageCount);
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void setPageLimit(int pageLimit) {
        if (searchResult.length == 0) { return; }
        this.pageLimit = pageLimit;
        maxPageCount = (searchResult.length - 1) / pageLimit;
        toFirstPage();
    }

    private void showSearchResult() {
        removeAllSearchResultTableRow();
        setTotalSearchedPagesNumberLabel(String.valueOf(searchResult.length));
        setPageLimit(gui.getPageLimit());
        if (searchResult.length == 0) {
            setCurrentPageNumberLabelZero();
        }
    }

    public void assignChildSpiders() {
        findPages();
        showSearchResult();
        gui.showSearchComplete();
    }

    // store search setting that like search class, search keyword and max search pages
    public static class SearchSettingHistory {
        String searchClass;
        String searchKeyword;
        int maxSearchLimit;

        SearchSettingHistory(String searchClass, String searchKeyword, int maxSearchPages) {
            this.searchClass = searchClass;
            this.searchKeyword = searchKeyword;
            this.maxSearchLimit = maxSearchPages;
        }

        public String toString() {
            return "class: " + searchClass + " keyword: " + searchKeyword + " max pages: " + maxSearchLimit;
        }

        private boolean isSameClass(SearchSettingHistory right) {
            return searchClass.equals(right.searchClass);
        }

        private boolean isSameKeyword(SearchSettingHistory right) {
            return searchKeyword.equals(right.searchKeyword);
        }

        private boolean isSameMaxLimit(SearchSettingHistory right) {
            return maxSearchLimit == right.maxSearchLimit;
        }

        public boolean equals(Object o) {
            if (o instanceof SearchSettingHistory) {
                SearchSettingHistory right = (SearchSettingHistory) o;
                return isSameClass(right) && isSameKeyword(right) && isSameMaxLimit(right);
            }
            return false;
        }

        public int hashCode() {
            return searchClass.hashCode() + searchKeyword.hashCode() + maxSearchLimit;
        }
    }

    public static class SearchResultHistory {
        Date date;
        WebPageInfo[] wpsInfo; // wps: webPages

        SearchResultHistory(Date date, WebPageInfo[] pagesInfo) {
            this.date = date;
            this.wpsInfo = pagesInfo;
        }
    }
}
