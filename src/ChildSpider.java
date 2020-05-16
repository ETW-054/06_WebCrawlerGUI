import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChildSpider extends Thread {
    private final ChildSpidersCommander commander;
    private final int spiderNumber;
    private static final Lock getPageLock = new ReentrantLock();
    private static final Object LOCK = new Object();

    public ChildSpider(ChildSpidersCommander motherSpider, int spiderNumber) {
        this.commander = motherSpider;
        this.spiderNumber = spiderNumber;
    }

    private void addToUsefulPages(PageInfo page) {
        Pattern pattern = Pattern.compile("(https://www.google.com/search\\?q=|" +
                "https://www.youtube.com/results\\?search_query=).*");
        Matcher matcher = pattern.matcher(page.link);

        if (matcher.find()) {
            commander.linksVisited.remove(page.link);
            return;
        }
        commander.addToUsefulPages(page);
    }

    public void search(String currentUrl) {
        SpiderLeg leg = new SpiderLeg(spiderNumber);

        if (!leg.isCrawl(currentUrl)) {
            commander.linksVisited.remove(currentUrl);
            return;
        }
        commander.addToVisitLinks(leg.getLinks(commander.searchClass));

        boolean isSuccess = leg.searchForWord(commander.searchKeyword);

        if (isSuccess) {
            System.out.println(String.format("Child " + spiderNumber + " *Success* Found word '%s' at %s",
                    commander.searchKeyword, currentUrl));
            addToUsefulPages(leg.getPageInfo());
        } else {
            System.out.println("Child " + spiderNumber + " *Fail* Can't found " + commander.searchKeyword);
        }
    }

    private void waitForPages() {
        synchronized (LOCK) {
            try {
                LOCK.wait();
            } catch (Exception ignored) { }
        }
    }

    private void wakeUpOtherChild() {
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
    }

    public void run() {
        while (commander.hasNotReachMaxSearchPages()) {
            getPageLock.lock();
            String currentUrl;
            if (commander.linksToVisit.isEmpty()) {
                getPageLock.unlock();
                waitForPages();
                continue;
            }
            currentUrl = commander.getNextUrl();
            getPageLock.unlock();

            search(currentUrl);
            wakeUpOtherChild();
        }
    }
}
