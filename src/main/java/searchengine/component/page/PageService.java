package searchengine.component.page;

import searchengine.dto.statistics.PageStatistic;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;


public interface PageService<PageEntity> {
    List<PageEntity> saveAll(List<PageEntity> pageList);
    List<PageEntity> getPagesBySite(SiteEntity site);

    void deleteAllPages();

    List<PageEntity> getPagesByIds(List<Integer> pageIds);

    PageEntity checkLinkInDB(String url, SiteEntity site) throws URISyntaxException, MalformedURLException;

    void deletePage(PageEntity page);

    void addToDB(List<PageStatistic> pageStatisticList, SiteEntity site);

    void save(PageEntity page);
}
