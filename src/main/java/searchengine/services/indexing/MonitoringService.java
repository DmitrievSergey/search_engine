package searchengine.services.indexing;

import searchengine.entity.SiteEntity;
import searchengine.services.scrabbing.LinkProcessor;
import searchengine.services.site.SiteService;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;



public interface MonitoringService extends Runnable {
    void monitoringIndexind(SiteService<SiteEntity> siteService, List<LinkProcessor> tasks, List<ForkJoinPool> listOfPools);
}
