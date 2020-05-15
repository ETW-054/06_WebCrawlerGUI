import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChildSpider extends Thread {
    private final MotherSpider motherSpider;
    private final int spiderNumber;
    private static final Lock getPageLock = new ReentrantLock();
    private static final Object LOCK = new Object();

    public ChildSpider(MotherSpider motherSpider, int spiderNumber) {
        this.motherSpider = motherSpider;
        this.spiderNumber = spiderNumber;
    }

    public void search(String currentUrl) {
        SpiderLeg leg = new SpiderLeg(spiderNumber);

        if (!leg.isCrawl(currentUrl)) {
            motherSpider.pagesVisited.remove(currentUrl);
            return;
        }
        motherSpider.addPagesToVisitPages(leg.getLinks(motherSpider.getSearchClass()));

        boolean isSuccess = leg.searchForWord(motherSpider.getSearchKeyword());

        if (isSuccess) {
            System.out.println(String.format("Child " + spiderNumber + " *Success* Found word '%s' at %s",
                    motherSpider.getSearchKeyword(), currentUrl));
            addThisPageToUsefulPages(leg.getPageInfo());
        } else {
            System.out.println("Child " + spiderNumber + " *Fail* Can't found " + motherSpider.getSearchKeyword());
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
        while (motherSpider.hasNotReachMaxSearchPages()) {
            getPageLock.lock();
            String currentUrl;
            if (motherSpider.pagesToVisit.isEmpty()) {
                getPageLock.unlock();
                waitForPages();
                continue;
            }
            currentUrl = motherSpider.getNextUrl();
            getPageLock.unlock();

            search(currentUrl);
            wakeUpOtherChild();
        }
    }

    public void addThisPageToUsefulPages(PageInfo page) {
        motherSpider.addToUsefulPages(page);
    }
}
