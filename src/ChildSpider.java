import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChildSpider extends Thread {
    private final MotherSpider motherSpider;
    private final int spiderNumber;
    private static final Lock lock = new ReentrantLock();

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
        motherSpider.addPagesToVisitPages(leg.getLinks());

        boolean isSuccess = leg.searchForWord(motherSpider.getSearchKeyword());

        if (isSuccess) {
            System.out.println(String.format("Child " + spiderNumber + " *Success* Found word '%s' at %s",
                    motherSpider.getSearchKeyword(), currentUrl));
            addToUsefulPages(leg.getPageInfo());
        } else {
            System.out.println("Child " + spiderNumber + " *Fail* Can't found " + motherSpider.getSearchKeyword());
        }

        //Object[] objects = { number++, null, currentUrl, isSuccess };
        //motherSpider.addSearchResultTableRowData(objects);
    }

    private void waitForPages() {
        try {
            //motherSpider.addWaitChild(this);
            this.wait();
        } catch (Exception ignored) { }
    }

    public void run() {
        while (motherSpider.isCommandContinueCrawl()) {
            String currentUrl;
            lock.lock();
            System.out.println(this.toString() + " get " + motherSpider.pagesToVisit.size());
            if (motherSpider.pagesToVisit.isEmpty()) {
                lock.unlock();
                waitForPages();
                continue;
            }
            currentUrl = motherSpider.getNextUrl();
            lock.unlock();
            search(currentUrl);
            motherSpider.wakeUpChild();
            //System.out.println("Child " + spiderNumber + ": " + motherSpider.getNextUrl());
        }
    }

    public void addToUsefulPages(PageInfo page) {
        motherSpider.addToUsefulPages(page);
    }
}
