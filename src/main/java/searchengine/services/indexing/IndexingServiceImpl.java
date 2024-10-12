package searchengine.services.indexing;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexindResponse;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.jsoup.JsoupService;
import searchengine.services.page.PageService;
import searchengine.services.scrabbing.FileService;
import searchengine.services.scrabbing.PageProcessor;
import searchengine.services.site.SiteService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@Getter
@Setter
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private MonitoringService monitoringService;
    private SiteService<SiteEntity> siteService;
    private JsoupService jsoupService;
    private CheckLinkService<CheckLinkEntity> checkLinkService;
    private PageService<PageEntity> pageService;
    private List<ForkJoinPool> listOfPools = new ArrayList<>();
    private List<Future<String>> results = new ArrayList<>();
    private List<PageProcessor> tasks = new ArrayList<>();


    private final int cores = Runtime.getRuntime().availableProcessors();
    ExecutorService poolExecutor;
    ScheduledExecutorService monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> monitoringResult = null;


    public IndexingServiceImpl(MonitoringService monitoringService, SitesList sitesList, CheckLinkService<CheckLinkEntity> checkLinkService
            , PageService<PageEntity> pageService, SiteService<SiteEntity> siteService
            , JsoupService jsoupService) {
        this.monitoringService = monitoringService;
        this.sitesList = sitesList;
        this.checkLinkService = checkLinkService;
        this.pageService = pageService;
        this.siteService = siteService;
        this.jsoupService = jsoupService;
    }


    @Override
    public IndexindResponse startIndexingAllSites() {

        List<SiteConfig> sitesConfig = sitesList.getSiteConfigs();
        if(isIndexingRunning.get()) {
            return new IndexindResponse(false, IndexindResponse.INDEXING_ALREADY_BEGIN);
        } else {
            isIndexingRunning.set(true);
            siteService.deleteAllSites();
            siteService.addAllSites();
        }

        final int countOfSites = sitesConfig.size();
        poolExecutor = Executors.newFixedThreadPool(countOfSites + 1);

        try {

            siteService.getAllSites().forEach(site -> {
                        ForkJoinPool pool = new ForkJoinPool();
                        PageProcessor task = new PageProcessor(jsoupService, site.getUrl(), site);
                        tasks.add(task);
                        listOfPools.add(pool);
                        SubmitPool submitPool = new SubmitPool(pool, task);
                        Future<String> future = poolExecutor.submit(submitPool);
                        results.add(future);
                        siteService.updateSite(site, Status.INDEXING, null);
                    }
            );
            MonitoringIndexing monitoring = new MonitoringIndexing(siteService, results);
            poolExecutor.submit(monitoring);
            poolExecutor.shutdown();
            return new IndexindResponse(true, null);
        } catch (Exception e) {
            siteService.getAllSites().forEach(site -> {
                siteService.updateSite(site, Status.FAILED, e.getLocalizedMessage());
            });
            return new IndexindResponse(false, IndexindResponse.INDEXING_FAILED);
        }
    }

    @Override
    public IndexindResponse stopIndexingAllSites() {
        if (!IndexingService.isIndexingRunning.get()) return new IndexindResponse(false, IndexindResponse.INDEXING_NOT_BEGIN);

        if (!isIndexingStopped.get()) {
            isIndexingStopped.set(true);
        }

        return new IndexindResponse(true, null);
    }

    @Override
    public IndexindResponse startIndexingPage(String url) {
        return null;
    }

}

