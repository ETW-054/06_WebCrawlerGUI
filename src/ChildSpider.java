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
        commander.addToVisitUrls(leg.getUrls());
        commander.addUsefulPage(leg.getWebPageInfo());
    }

    public void run() {
        try {
            search();
        } catch (Exception ignore) { }

        boolean isRemoved;
        do {
            synchronized (commander.LOCK) {
                isRemoved = commander.childSpiders.remove(this);
            }
        } while (!isRemoved);
    }
}
