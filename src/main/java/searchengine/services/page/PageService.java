package searchengine.services.page;

import searchengine.config.SiteConfig;
import searchengine.entity.SiteEntity;

import java.sql.SQLException;
import java.util.List;


public interface PageService<PageEntity> {
    PageEntity saveSitePage(PageEntity page) throws SQLException;

    Long getCount();

    Long getCountBySite(SiteEntity site);

    void saveListOfSitePage(List<PageEntity> pageEntityList);
    void updateSitePage(PageEntity page);
    void deleteAllSitePage(SiteConfig siteConfig);
    boolean getAllBySiteIdAndPath(PageEntity page);
    boolean getAllBySiteIdAndPath(int siteId,  String path);
}
