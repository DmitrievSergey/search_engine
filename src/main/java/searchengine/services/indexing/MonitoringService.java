package searchengine.services.indexing;

import searchengine.entity.SiteEntity;
import searchengine.services.site.SiteService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RunnableFuture;

public interface MonitoringService extends Runnable {
    void monitoringIndexind(List<Future<String>> results, SiteService<SiteEntity> siteService);
}
