public class ChildSpider extends Thread {
    private final ChildSpidersCommander commander;
    private final String currentUrl;

    public ChildSpider(ChildSpidersCommander motherSpider, String link) {
        this.commander = motherSpider;
        this.currentUrl = link;
    }

    private void addUsefulPage(PageInfo newPage) {
        commander.addUsefulPage(newPage);
    }

    public void search() {
        SpiderLeg leg = new SpiderLeg(this.toString(), commander.searchKeyword);

        if (!leg.isCrawl(currentUrl)) {
            return;
        }
        commander.addToVisitLinks(leg.getLinks(commander.searchClass));

        PageInfo page = leg.getPageInfo();

        if (page.keywordCount != 0) {
            addUsefulPage(page);
        }
    }

    public void run() {
        search();
        synchronized (commander.LOCK) {
            commander.childSpiders.remove(this);
        }
    }
}
