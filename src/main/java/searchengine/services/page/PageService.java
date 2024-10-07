package searchengine.services.page;

import searchengine.config.SiteConfig;
import searchengine.entity.PageEntity;

import java.util.Set;


public interface PageService<PageEntity> {
    PageEntity saveSitePage(PageEntity page);
    void saveListOfSitePage(Set<PageEntity> pageEntityList);
    void updateSitePage(PageEntity page);
    void deleteAllSitePage(SiteConfig siteConfig);
    boolean getAllBySiteIdAndPath(PageEntity page);
    boolean getAllBySiteIdAndPath(int siteId,  String path);
}
