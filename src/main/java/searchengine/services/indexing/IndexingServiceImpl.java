package searchengine.services.indexing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.component.MonitoringIndexingStatus;
import searchengine.component.MonitoringPageIndexing;
import searchengine.component.MonitoringService;
import searchengine.component.MonitoringSiteIndexing;
import searchengine.config.SitesListConfig;
import searchengine.dto.indexing.IndexindResponse;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.component.site.SiteService;
import searchengine.component.index.IndexService;
import searchengine.component.lemma.LemmaService;
import searchengine.component.page.PageService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
@Setter
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);

    private final SitesListConfig sitesList;
    private final MonitoringService monitoringService;
    private final SiteService<SiteEntity> siteService;
    private final PageService<PageEntity> pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;

    private List<Future<?>> siteRunnableList = new ArrayList<>();

    ExecutorService indexingExecutor;
    ExecutorService statusExecutor;

    @Override
    public IndexindResponse startIndexingAllSites() {
        //Проверить статус индексации
        if (isIndexingRunning.get()) {
            return new IndexindResponse(false, IndexindResponse.INDEXING_ALREADY_BEGIN);
        } else {
            isIndexingRunning.set(true);
            deleteAllSites();
            statusExecutor = Executors.newSingleThreadExecutor();
            indexingExecutor = Executors.newFixedThreadPool(CORES);
            sitesList.getSites().parallelStream().forEach(siteConfig -> {
                logger.info("Start indexing site: " + siteConfig.getName() + " url - " + siteConfig.getUrl());
                MonitoringService monitoring = new MonitoringSiteIndexing(siteService, pageService,
                        indexService, lemmaService, siteConfig);
                Future<?> future = indexingExecutor.submit(monitoring);
                siteRunnableList.add(future);
            });
            statusExecutor.execute(new MonitoringIndexingStatus(siteRunnableList, sitesList.getSites().size()));
            indexingExecutor.shutdown();
            statusExecutor.shutdown();

        }

        return new IndexindResponse(true, null);
    }



    @Override
    public IndexindResponse stopIndexingAllSites() {
        if (!IndexingService.isIndexingRunning.get())
            return new IndexindResponse(false, IndexindResponse.INDEXING_NOT_BEGIN);

        if (!isIndexingStopped.get()) {
            isIndexingStopped.set(true);
            Thread.currentThread().interrupt();
        }


        return new IndexindResponse(true, null);
    }

    @Override
    public IndexindResponse startIndexingPage(String url) {
        if(IndexingService.isIndexingRunning.get()) {
            return new IndexindResponse(true, null);
        }
        IndexingService.isIndexingRunning.set(true);
        AtomicBoolean isCorrectUrl = checkUrl(url);
        if(!isCorrectUrl.get()) new IndexindResponse(true, IndexindResponse.PAGE_OUT_OF_SITES_CONFIG);
        sitesList.getSites().parallelStream().forEach(siteConfig -> {
            if(url.contains(siteConfig.getUrl())) {
                statusExecutor = Executors.newSingleThreadExecutor();
                indexingExecutor = Executors.newSingleThreadExecutor();
                MonitoringPageIndexing monitoring = new MonitoringPageIndexing(
                        url,
                        siteService, pageService,
                        indexService, lemmaService, siteConfig);
                Future<?> future = indexingExecutor.submit(monitoring);
                siteRunnableList.add(future);
            }
        });
        statusExecutor.execute(new MonitoringIndexingStatus(siteRunnableList, 1));
        indexingExecutor.shutdown();
        statusExecutor.shutdown();


        return new IndexindResponse(true, null);
    }

    private AtomicBoolean checkUrl(String url) {
        AtomicBoolean isCorrectUrl = new AtomicBoolean(false);
        sitesList.getSites().parallelStream().forEach(siteConfig -> {
            if(url.contains(siteConfig.getUrl())) isCorrectUrl.set(true);
        });
        return isCorrectUrl;
    }

    private void deleteAllSites() {
        indexService.deleteAllIndexes();
        lemmaService.deleteAllLemmas();
        pageService.deleteAllPages();
        siteService.deleteAll();
    }

}




