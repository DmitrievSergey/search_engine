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
        log.info("Счетчик количества выполненных задач равен = " + completedTasks);
        if(completedTasks == countSites) {
            log.info("Зашли внутрь if");
            monitoringResult.cancel(true);
        }
        while (completedTasks != countSites) {
            log.info("Счетчик количества сайтов равен = " + countSites);
            log.info("Счетчик количества выполненных задач равен = " + completedTasks);
            if (IndexingService.isIndexingStopped.get()) {
                break;
            }
            for (Future<String> future : results) {
                try {
                    if (future.isDone()) {
                        log.info("Результат future get - isDone " + future.get());
                        completedTasks++;
                        SiteEntity site = siteService.findSiteByUrl((String) future.get());
                        site.setStatus(Status.INDEXED);
                        site.setStatusTime(LocalDateTime.now());
                        siteService.updateSite(site);
                        log.info("Счетчик количества выполненных задач равен = " + completedTasks);
                    }
                    if(future.isCancelled()) {
                        completedTasks++;
                        log.info("Результат future get - " + future.get());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void run() {
        monitoringIndexind(results, siteService);
    }
}
