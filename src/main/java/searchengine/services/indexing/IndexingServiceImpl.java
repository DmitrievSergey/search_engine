package searchengine.services.indexing;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.SafeForkJoinWorkerThreadFactory;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesListConfig;
import searchengine.dto.indexing.IndexindResponse;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.jsoup.JsoupService;
import searchengine.services.page.PageService;
import searchengine.services.scrabbing.LinkProcessor;
import searchengine.services.site.SiteService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@Getter
@Setter
public class IndexingServiceImpl implements IndexingService {

    private final SitesListConfig sitesList;
    private MonitoringService monitoringService;
    private SiteService<SiteEntity> siteService;
    private JsoupService jsoupService;
    private CheckLinkService checkLinkService;
    private PageService<PageEntity> pageService;
    private List<ForkJoinPool> listOfPools = new ArrayList<>();
    private List<Future<String>> linksResults = new ArrayList<>();
    private List<LinkProcessor> tasks = new ArrayList<>();
    private List<Future<Integer>> pagesResults = new ArrayList<>();
    private List<SubmitPool> listOfSubmitPools = new ArrayList<>();


    private final int cores = Runtime.getRuntime().availableProcessors();
    ExecutorService poolExecutor;
    ExecutorService monitoringExecutor;



    public IndexingServiceImpl(MonitoringService monitoringService, SitesListConfig sitesList, CheckLinkService checkLinkService
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

        List<SiteConfig> sitesConfig = sitesList.getSites();
        if (isIndexingRunning.get()) {
            return new IndexindResponse(false, IndexindResponse.INDEXING_ALREADY_BEGIN);
        } else {
            isIndexingRunning.set(true);
            siteService.deleteAllSites();

            siteService.addAllSites();
        }

        final int countOfSites = sitesConfig.size();
        poolExecutor = Executors.newFixedThreadPool(countOfSites);
        monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
        ExecutorService sitePoolExecutor = Executors.newFixedThreadPool(4);

        try {
            siteService.getAllSites().parallelStream().forEach(site -> {
                LinkProcessor task = new LinkProcessor(jsoupService, site.getUrl(), site);
                tasks.add(task);
                siteService.updateSite(site, Status.INDEXING, null);
            });
            ForkJoinPool poolSite = new ForkJoinPool();
            tasks.parallelStream().forEach(poolSite::execute);
            MonitoringIndexing monitoring = new MonitoringIndexing(siteService, tasks, listOfPools);
            monitoringExecutor.submit(monitoring);
            monitoringExecutor.shutdown();

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
        if (!IndexingService.isIndexingRunning.get())
            return new IndexindResponse(false, IndexindResponse.INDEXING_NOT_BEGIN);

        if (!isIndexingStopped.get()) {
            isIndexingStopped.set(true);
        }


        return new IndexindResponse(true, null);
    }

    @Override
    public IndexindResponse startIndexingPage(String url) {
        SiteConfig siteConfig = sitesList.existInConfig(url);
        if (siteConfig == null)
            return new IndexindResponse(false, IndexindResponse.PAGE_OUT_OF_SITES_CONFIG);

        if (!siteService.existInDB(url)) siteService.addAllSites();
        SiteEntity site = siteService.findSiteByUrl(siteConfig.getUrl());
        ExecutorService pageExecutor = Executors.newSingleThreadScheduledExecutor();
        ForkJoinPool pagePool = new ForkJoinPool();
        pagePool.execute(() -> jsoupService.checkAndAddToIndexPage(url, site));
        return new IndexindResponse(true, null);
    }

}


