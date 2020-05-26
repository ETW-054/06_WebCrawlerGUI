import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static java.lang.Integer.min;

public class MotherSpider {
    private final WebCrawlerGUI gui;
    private final ConcurrentHashMap<SearchSettingHistory, SearchResultHistory> searchHistory = new ConcurrentHashMap<>();
    private SearchResultHistory searchResult;
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

        // 將 Set 轉成 Array
        Set<WebPageInfo> resultTemp = commander.getUsefulPages();
        WebPageInfo[] searchResultTemp = new WebPageInfo[resultTemp.size()];
        try {
            resultTemp.toArray(searchResultTemp);
        } catch (Exception ignore) { }
        Arrays.sort(searchResultTemp);

        return searchResultTemp;
    }

    private boolean isUpdateToDate(Date historyDate) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String history = sdFormat.format(historyDate);
        String current = sdFormat.format(new Date());
        try {
            long hTime = sdFormat.parse(history).getTime();
            long cTime = sdFormat.parse(current).getTime();
            long diffTime = (cTime - hTime) / 1000; // ms to s
            return diffTime < 300; // 大於5分鐘就重找
        } catch (Exception e) {
            return false;
        }
    }

    private void findPages() {
        SearchSettingHistory ssh = new SearchSettingHistory(getSearchClass(), getSearchKeyword());

        if (searchHistory.containsKey(ssh)) {
            SearchResultHistory srh =  searchHistory.get(ssh);
            // 時間為最新時間 && 歷史最大搜尋限制大於等於目前最大限制+5 && 歷史最大搜尋限制大於等於歷史搜尋數量
            if (isUpdateToDate(srh.date) && ((srh.maxSearchLimit + 5 >= getMaxSearchLimit()) ||
                    (srh.maxSearchLimit >= srh.wpsInfo.length))) {
                searchResult = srh;
                return;
            }
        }

        searchResult = new SearchResultHistory(new Date(), searchWebPages(), getMaxSearchLimit());
        searchHistory.put(ssh, searchResult);
    }

    private void setTotalSearchedPagesNumberLabel(String text) {
        gui.setTotalSearchedWebPagesLabel(text);
    }

    private void setSearchResultTable(int pageCount) {
        currentPageCount = pageCount;
        WebPageInfo[] result = searchResult.wpsInfo;
        int maxLimit = min((currentPageCount + 1) * pageLimit, result.length);

        for (int i = currentPageCount * pageLimit; i < maxLimit; i++) {
            Object[] objects = { i + 1, result[i].title, result[i].url, result[i].info, (int)result[i].weight };
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
        if (searchResult.wpsInfo.length == 0) { return; }
        removeAllSearchResultTableRow();
        setSearchResultTable(0);
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void toFrontPage() {
        if (searchResult.wpsInfo.length == 0) { return; }
        removeAllSearchResultTableRow();
        if (currentPageCount == 0) {
            setSearchResultTable(0);
        } else {
            setSearchResultTable(--currentPageCount);
        }
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void toNextPage() {
        if (searchResult.wpsInfo.length == 0) { return; }
        removeAllSearchResultTableRow();
        if (currentPageCount == maxPageCount) {
            setSearchResultTable(maxPageCount);
        } else {
            setSearchResultTable(++currentPageCount);
        }
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void toLastPage() {
        if (searchResult.wpsInfo.length == 0) { return; }
        removeAllSearchResultTableRow();
        setSearchResultTable(maxPageCount);
        setCurrentPageNumberLabel(currentPageCount + 1);
    }

    public void setPageLimit(int pageLimit) {
        if (searchResult == null) { return; }
        if (searchResult.wpsInfo.length == 0) { return; }
        this.pageLimit = pageLimit;
        maxPageCount = (searchResult.wpsInfo.length - 1) / pageLimit;
        toFirstPage();
    }

    private void setSnapshotLabel() {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        gui.setSnapshotLabel(sdFormat.format(searchResult.date));
    }

    private void showSearchResult() {
        removeAllSearchResultTableRow();
        setTotalSearchedPagesNumberLabel(String.valueOf(searchResult.wpsInfo.length));
        setPageLimit(gui.getPageLimit());
        setSnapshotLabel();
        if (searchResult.wpsInfo.length == 0) {
            setCurrentPageNumberLabelZero();
        }
    }

    public void assignChildSpiders() {
        findPages();
        showSearchResult();
        gui.showInfo(searchResult.wpsInfo.length + " results found!");
    }

    // 用來存 search class, search keyword
    public static class SearchSettingHistory {
        String searchClass;
        String searchKeyword;

        SearchSettingHistory(String searchClass, String searchKeyword) {
            this.searchClass = searchClass;
            this.searchKeyword = searchKeyword;
        }

        public String toString() {
            return "class: " + searchClass + " keyword: " + searchKeyword;
        }

        private boolean isSameClass(SearchSettingHistory right) {
            return searchClass.equals(right.searchClass);
        }

        private boolean isSameKeyword(SearchSettingHistory right) {
            return searchKeyword.equals(right.searchKeyword);
        }

        public boolean equals(Object o) {
            if (o instanceof SearchSettingHistory) {
                SearchSettingHistory right = (SearchSettingHistory) o;
                return isSameClass(right) && isSameKeyword(right);
            }
            return false;
        }

        public int hashCode() {
            return searchClass.hashCode() + searchKeyword.hashCode();
        }
    }

    public static class SearchResultHistory {
        Date date;
        WebPageInfo[] wpsInfo; // wps: webPages
        int maxSearchLimit;

        SearchResultHistory(Date date, WebPageInfo[] pagesInfo, int maxSearchPages) {
            this.date = date;
            this.wpsInfo = pagesInfo;
            this.maxSearchLimit = maxSearchPages;
        }
    }
}
