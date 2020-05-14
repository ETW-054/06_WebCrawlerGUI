public class ChildSpider extends Thread {
    private final MotherSpider motherSpider;
    private final int spiderNumber;

    public ChildSpider(MotherSpider motherSpider, int spiderNumber) {
        this.motherSpider = motherSpider;
        this.spiderNumber = spiderNumber;
    }

    public void search() {
        String currentUrl = motherSpider.getNextUrl();

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
            motherSpider.addToUsefulPages(leg.getPageInfo());
        } else {
            System.out.println("Child " + spiderNumber + " *Fail* Can't found " + motherSpider.getSearchKeyword());
        }

        //Object[] objects = { number++, null, currentUrl, isSuccess };
        //motherSpider.addSearchResultTableRowData(objects);
    }

    public void run() {
        while (motherSpider.isCommandContinueCrawl()) {
            if (motherSpider.pagesToVisit.isEmpty()) {
                try {
                    motherSpider.addWaitChild(this);
                    this.wait();
                } catch (Exception ignored) { }
                continue;
            }
            search();
            motherSpider.wakeUpChild();
            //System.out.println("Child " + spiderNumber + ": " + motherSpider.getNextUrl());
        }
    }

    public void addToUsefulPages(PageInfo page) {
        motherSpider.addToUsefulPages(page);
    }
}
