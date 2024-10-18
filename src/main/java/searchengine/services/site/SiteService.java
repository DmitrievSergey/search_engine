package searchengine.services.site;

import searchengine.entity.Status;


import java.util.List;

public interface SiteService<SiteEntity> {
    void addAllSites();

    boolean existInDB(String url);

    SiteEntity saveSite(SiteEntity site);

    SiteEntity updateSite(SiteEntity site, Status status, String error);

    List<SiteEntity> getAllSites ();

    SiteEntity findSiteByUrl(String url);
    SiteEntity findSiteById(int id);

    Long getSitesCount();

    void deleteAllSites();
}
