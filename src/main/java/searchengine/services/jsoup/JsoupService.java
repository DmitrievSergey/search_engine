package searchengine.services.jsoup;

import org.jsoup.Connection;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

import java.io.IOException;
import java.util.Set;

public interface JsoupService {
    Set<String> getUrlsSetFromUrl(String url, SiteEntity site);
    PageEntity getPageData(String url, SiteEntity site);
}
