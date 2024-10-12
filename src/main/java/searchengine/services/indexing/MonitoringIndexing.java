package searchengine.services.indexing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class MonitoringIndexing implements MonitoringService {
    private SiteService<SiteEntity> siteService;
    private List<Future<String>> results;
    private ScheduledFuture<?> monitoringResult;
    private int completedTasks = 0;

    public MonitoringIndexing(SiteService<SiteEntity> siteService, List<Future<String>> results) {
        this.siteService = siteService;
        this.results = results;
    }

    public void monitoringIndexind(List<Future<String>> results, SiteService<SiteEntity> siteService) {
        int countSites = siteService.getAllSites().size();


        while (completedTasks != countSites) {

            if (IndexingService.isIndexingStopped.get()) {
                for(Iterator<Future<String>> iterator = results.iterator(); iterator.hasNext();) {
                    Future<String> future = iterator.next();
                    future.cancel(true);
                    completedTasks++;
                }
            }
            for(Iterator<Future<String>> iterator = results.iterator(); iterator.hasNext();) {
                Future<String> future = iterator.next();
                if (future.isDone()) {
                    completedTasks++;
                    SiteEntity site = null;
                    try {
                        site = siteService.findSiteByUrl(future.get());
                        siteService.updateSite(site, Status.INDEXED, null);
                        iterator.remove();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                }
                if(future.isCancelled()) {
                    completedTasks++;
                    SiteEntity site = null;
                    try {
                        site = siteService.findSiteByUrl((String) future.get());
                        siteService.updateSite(site, Status.FAILED, null);
                        iterator.remove();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if(completedTasks == countSites) {
            log.info("Зашли внутрь if");
            IndexingService.isIndexingRunning.set(false);
            //monitoringResult.cancel(true);
        }

    }

    @Override
    public void run() {
        monitoringIndexind(results, siteService);
    }

}
