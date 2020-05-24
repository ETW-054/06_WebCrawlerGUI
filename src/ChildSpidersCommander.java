import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.lang.Thread.sleep;

public class ChildSpidersCommander extends Thread {
    protected final Set<ChildSpider> childSpiders = new HashSet<>();
    protected final Set<String> linksVisited = new HashSet<>();
    protected final List<String> linksToVisit = new LinkedList<>();
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

    private void setToVisitPages() {
        linksToVisit.addAll(wpCommand.getDefaultUrl(searchKeyword));
    }

    public void addToVisitLinks(List<String> links) {
        synchronized (LOCK) {
            for (String link : links) {
                if (!linksToVisit.contains(link)) {
                    linksToVisit.add(link);
                }
            }
        }
    }

    private boolean isDefaultLink(String link) {
        return wpCommand.isDefaultUrl(link);
    }

    public void addUsefulPage(WebPageInfo newPage) {
        if (isDefaultLink(newPage.link)) {
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
        String nextUrl;
        nextUrl = linksToVisit.remove(0);

        while (linksVisited.contains(nextUrl)) {
            nextUrl = linksToVisit.remove(0);
        }

        if (!isDefaultLink(nextUrl)) {
            linksVisited.add(nextUrl);
        }
        return nextUrl;
    }

    private void create() {
        do {
            while (childSpiders.size() < maxThreads && !linksToVisit.isEmpty() && linksVisited.size() < maxPages) {
                synchronized (LOCK) {
                    ChildSpider cs = new ChildSpider(this, getNextUrl());
                    childSpiders.add(cs);
                    cs.start();
                }
            }
            try {
                sleep(100);
            } catch (Exception ignore) { }
        } while ((!childSpiders.isEmpty() || !linksToVisit.isEmpty()) && linksVisited.size() < maxPages);
    }

    private void waitForAllChildSpiderStop() {
        try {
            while (!childSpiders.isEmpty()) {
                sleep(100);
            }
        } catch (Exception ignored) { }
    }

    public void run() {
        setToVisitPages();
        create();
        waitForAllChildSpiderStop();
    }
}
