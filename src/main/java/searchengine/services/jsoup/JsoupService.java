package searchengine.services.jsoup;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public interface JsoupService {
    public static ConcurrentSkipListSet<String> siteLinkSet = new ConcurrentSkipListSet<String>();
    Set<String> getUrlsSetFromUrl(String url, SiteEntity site);
    void checkAndAddToIndexPage(String url, SiteEntity site);
}
