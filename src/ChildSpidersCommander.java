import java.util.*;

import static java.lang.Thread.sleep;

public class ChildSpidersCommander extends Thread {
    protected final Set<ChildSpider> childSpiders = new HashSet<>();
    protected final Set<String> urlsVisited = new HashSet<>();
    protected final List<String> urlsToVisit = new LinkedList<>();
    protected final Set<WebPageInfo> usefulPages = new HashSet<>();

    protected final WebPageCommand wpCommand;
    protected final String searchKeyword;
    protected final int maxPages;
    protected final int maxThreads;
    protected final Object LOCK = new Object();

    public ChildSpidersCommander(String searchKeyword, String searchClass, int maxPages, int maxThreads) {
        this.searchKeyword = searchKeyword;
        this.wpCommand = WebPageCommand.valueOf(searchClass);
        this.maxPages = maxPages;
        this.maxThreads = maxThreads;
    }

    public Set<WebPageInfo> getUsefulPages() {
        return usefulPages;
    }

    private void setToVisitUrls() {
        urlsToVisit.addAll(wpCommand.getDefaultUrl(searchKeyword));
    }

    public void addToVisitLinks(List<String> urls) {
        synchronized (LOCK) {
            for (String url : urls) {
                if (!urlsToVisit.contains(url)) {
                    urlsToVisit.add(url);
                }
            }
        }
    }

    private boolean isDefaultUrl(String url) {
        return wpCommand.isDefaultUrl(url);
    }

    public void addUsefulPage(WebPageInfo newPage) {
        if (isDefaultUrl(newPage.url)) {
            return;
        }

        for (WebPageInfo page:usefulPages) {
            if (newPage.equals(page)) {
                return;
            }
        }
        usefulPages.add(newPage);
    }

    public String getNextUrl() {
        if (urlsToVisit.isEmpty()) { return ""; }

        String nextUrl;
        nextUrl = urlsToVisit.remove(0);

        while (urlsVisited.contains(nextUrl)) {
            if (urlsToVisit.isEmpty()) { return ""; }
            nextUrl = urlsToVisit.remove(0);
        }

        if (!isDefaultUrl(nextUrl)) {
            urlsVisited.add(nextUrl);
        }
        return nextUrl;
    }

    private void create() {
        do {
            while (childSpiders.size() < maxThreads && !urlsToVisit.isEmpty() && urlsVisited.size() < maxPages) {
                synchronized (LOCK) {
                    ChildSpider cs = new ChildSpider(this, getNextUrl());
                    childSpiders.add(cs);
                    cs.start();
                }
            }
            try {
                sleep(100);
            } catch (Exception ignore) { }
        } while ((!childSpiders.isEmpty() || !urlsToVisit.isEmpty()) && urlsVisited.size() < maxPages);
    }

    private void waitForAllChildSpiderStop() {
        try {
            while (!childSpiders.isEmpty()) {
                sleep(100);
            }
        } catch (Exception ignored) { }
    }

    public void run() {
        setToVisitUrls();
        create();
        waitForAllChildSpiderStop();
    }
}
