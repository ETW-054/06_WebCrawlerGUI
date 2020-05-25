public class ChildSpider extends Thread {
    private final ChildSpidersCommander commander;
    private final String currentUrl;

    public ChildSpider(ChildSpidersCommander motherSpider, String url) {
        this.commander = motherSpider;
        this.currentUrl = url;
    }

    public void search() {
        SpiderLeg leg = new SpiderLeg(commander.wpCommand, commander.searchKeyword);

        if (!leg.isCrawl(currentUrl)) {
            return;
        }
        commander.addToVisitLinks(leg.getUrls());
        commander.addUsefulPage(leg.getWebPageInfo());
    }

    public void run() {
        search();
        synchronized (commander.LOCK) {
            commander.childSpiders.remove(this);
        }
    }
}
