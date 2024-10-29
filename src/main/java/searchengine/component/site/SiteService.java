package searchengine.component.site;

import searchengine.config.SiteConfig;
import searchengine.entity.SiteEntity;

import java.util.List;

public interface SiteService<SiteEntity> {
    SiteEntity findSiteByUrl(String url);
    SiteEntity save(SiteEntity site);
    void delete(SiteEntity site);
    SiteEntity setFailedStatus(SiteEntity site, String error);
    SiteEntity setIndexedStatus(SiteEntity site);
    void deleteAll();

    SiteEntity findSiteBySiteId(Integer id);

    SiteEntity addSiteData(SiteConfig siteConfig);

    List<SiteEntity> findSitesWithStatusIndexed();
}
