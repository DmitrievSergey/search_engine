package searchengine.services.indexing;

import searchengine.entity.SiteEntity;
import searchengine.services.site.SiteService;

import java.util.List;
import java.util.concurrent.Future;


public interface MonitoringService extends Runnable {
    void monitoringIndexind(List<Future<String>> results, SiteService<SiteEntity> siteService);
}
