import java.util.*;

public class ChildSpidersCommander {
    private final Set<ChildSpider> childSpiders = new HashSet<>();
    protected final Set<String> linksVisited = new HashSet<>();
    protected final List<String> linksToVisit = new LinkedList<>();
    protected final Set<PageInfo> usefulPages = new HashSet<>();

    protected final String searchClass;
    protected final String searchKeyword;
    protected final int maxPages;
    protected final int maxThreads;

    public ChildSpidersCommander(String searchKeyword, String searchClass, int maxPages, int maxThreads) {
        this.searchKeyword = searchKeyword;
        this.searchClass = searchClass;
        this.maxPages = maxPages;
        this.maxThreads = maxThreads;
    }

    private List<String> getNewsUrl() {
        return new LinkedList<>() {{
            add("https://www.google.com/search?q="+ searchKeyword + "&safe=strict&tbm=nws&start=0");
            add("https://www.google.com/search?q="+ searchKeyword + "&safe=strict&tbm=nws&start=10");
            add("https://www.google.com/search?q="+ searchKeyword + "&safe=strict&tbm=nws&start=20");
        }};
    }

    private List<String> getYoutubeUrl() {
        return new LinkedList<>() {{
            add("https://www.youtube.com/results?search_query="+ searchKeyword);
        }};
    }

    private List<String> getShoppingUrl() {
        return new LinkedList<>() {{
            add("https://www.youtube.com/results?search_query="+ searchKeyword);
        }};
    }

    private void setToVisitPages() {
        if (searchClass.equals("新聞")) {
            linksToVisit.addAll(getNewsUrl());
        } else if (searchClass.equals("Youtube")) {
            linksToVisit.addAll(getYoutubeUrl());
        } else if (searchClass.equals("購物")) {
            linksToVisit.addAll(getShoppingUrl());
        }
    }

    // From GUI
    private boolean isNotMatchMaxThreads() {
        return childSpiders.size() < maxThreads;
    }

    // From GUI
    private boolean isNotMatchMaxPages() {
        return childSpiders.size() < maxPages;
    }

    private void createChildSpiders() {
        int childCount = 0;
        while (isNotMatchMaxThreads() && isNotMatchMaxPages()) {
            childSpiders.add(new ChildSpider(this, childCount++));
        }
    }

    private void commandChildSpiderStartCrawl() {
        for (ChildSpider cs : childSpiders) {
            cs.start();
        }
    }

    private void waitForAllChildSpiderStop() {
        try {
            for (ChildSpider cs : childSpiders) {
                cs.join();
            }
        } catch (Exception ignored) { }
    }

    private void removeChildSpider() {
        childSpiders.clear();
    }

    public String getNextUrl() {
        String nextUrl = linksToVisit.remove(0);

        while (linksVisited.contains(nextUrl)) {
            nextUrl = linksToVisit.remove(0);
        }
        linksVisited.add(nextUrl);

        return nextUrl;
    }

    public boolean hasNotReachMaxSearchPages() {
        return linksVisited.size() < maxPages;
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

    public void addToVisitLinks(List<String> pages) {
        linksToVisit.addAll(pages);
    }

    public Set<PageInfo> getUsefulPages() {
        return usefulPages;
    }

    public void execute() {
        setToVisitPages();
        createChildSpiders();
        commandChildSpiderStartCrawl();
        waitForAllChildSpiderStop();
        removeChildSpider();
    }
}
