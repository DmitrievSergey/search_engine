package searchengine.services.indexing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.services.jsoup.JsoupService;
import searchengine.services.scrabbing.LinkProcessor;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class MonitoringIndexing implements MonitoringService {
    private SiteService<SiteEntity> siteService;
    private List<LinkProcessor> tasks;
    private List<ForkJoinPool> listOfPools;

    private int completedTasks = 0;


    public MonitoringIndexing(SiteService<SiteEntity> siteService, List<LinkProcessor> tasks, List<ForkJoinPool> listOfPools) {
        this.siteService = siteService;
        this.tasks = tasks;
        this.listOfPools = listOfPools;
    }

    public void monitoringIndexind(SiteService<SiteEntity> siteService, List<LinkProcessor> tasks, List<ForkJoinPool> listOfPools) {
        int countSites = siteService.getAllSites().size();

        while (completedTasks != countSites) {
            for (Iterator<LinkProcessor> iterator = tasks.iterator(); iterator.hasNext(); ) {
                LinkProcessor task = iterator.next();
                if (task.isCompletedNormally()) {
                    completedTasks++;
                    SiteEntity site = null;
                    site = siteService.findSiteByUrl(task.getUrl());
                    siteService.updateSite(site, Status.INDEXED, null);
                    iterator.remove();

                }
                if (task.isCompletedAbnormally()) {
                    completedTasks++;
                    SiteEntity site = null;

                    site = siteService.findSiteByUrl(task.getUrl());
                    siteService.updateSite(site, Status.FAILED, null);
                    iterator.remove();

                }
                if (IndexingService.isIndexingStopped.get()) {
                    task.cancel(true);
                }
            }
        }
        if (completedTasks == countSites) {
            log.info("Зашли внутрь if");
//            JsoupService.siteLinkSet.clear();
            IndexingService.isIndexingRunning.set(false);
        }

    }

    @Override
    public void run() {
        monitoringIndexind(siteService, tasks, listOfPools);
    }

}
