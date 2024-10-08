package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
@Slf4j
@AllArgsConstructor
@Service
public class MonitoringIndexing implements MonitoringService {
    private SiteService<SiteEntity> siteService;
    List<Future<String>> results;

    public void monitoringIndexind(List<Future<String>> results, SiteService<SiteEntity> siteService) {
    int completedTasks = 0;
    int countSites = siteService.getAllSites().size();
    while(completedTasks != countSites)
        if(IndexingService.isIndexingStopped.get()) {
            break;
        };
        for (Future<?> future : results) {
            try {
                if (future.isDone()) {
                    log.info("Результат future get - " + future.get());
                    completedTasks++;
                    SiteEntity site = siteService.findSiteByUrl((String) future.get());
                    site.setStatus(Status.INDEXED);
                    site.setStatusTime(LocalDateTime.now());
                    siteService.updateSite(site);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        monitoringIndexind(results, siteService);
    }
}
