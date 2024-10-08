package searchengine.services.site;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;


import java.util.List;

public interface SiteService<SiteEntity> {

    SiteEntity saveSite(SiteEntity site);

    SiteEntity updateSite(SiteEntity site);

    List<SiteEntity> getAllSites ();

    SiteEntity findSiteByUrl(String url);

    void deleteAllSites();
}
