package searchengine.services.site;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import searchengine.config.SitesList;
import searchengine.entity.Status;


import java.util.List;

public interface SiteService<SiteEntity> {
    void addAllSites();

    SiteEntity saveSite(SiteEntity site);

    SiteEntity updateSite(SiteEntity site, Status status, String error);

    SiteEntity updateSite(SiteEntity site, Status status);

    List<SiteEntity> getAllSites ();

    SiteEntity findSiteByUrl(String url);

    void deleteAllSites();
}
