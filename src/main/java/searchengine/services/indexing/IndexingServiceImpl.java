package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexindResponse;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.page.PageService;
import searchengine.services.scrabbing.PageProcessor;
import searchengine.services.scrabbing.ScrubbingService;
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

    private SitesList sitesList;
    private SiteService<SiteEntity> siteService;
    private ScrubbingService scrubbingService;
    private CheckLinkService<CheckLinkEntity> checkLinkService;
    private PageService<PageEntity> pageService;
    private List<ForkJoinPool> listOfPools = new ArrayList<>();
    private List<Future<?>> results = new ArrayList<>();
    private List<PageProcessor> tasks = new ArrayList<>();
    private List<Thread> threads = new ArrayList<>();
    public static AtomicBoolean isIndexingStopped = new AtomicBoolean(false);
    private final int cores = Runtime.getRuntime().availableProcessors();


    public IndexingServiceImpl(SitesList sitesList, CheckLinkService<CheckLinkEntity> checkLinkService
            , PageService<PageEntity> pageService, SiteService<SiteEntity> siteService
            , ScrubbingService scrubbingService) {
        this.sitesList = sitesList;
        this.checkLinkService = checkLinkService;
        this.pageService = pageService;
        this.siteService = siteService;
        this.scrubbingService = scrubbingService;
    }


    @Override
    public IndexindResponse startIndexingAllSites() {

        List<SiteEntity> siteEntities = siteService.getAllSites();
        if (!siteEntities.isEmpty()
                && !siteEntities.stream().
                filter(siteEntity -> siteEntity.getStatus().equals(Status.INDEXING)).toList().isEmpty()) {
            return new IndexindResponse(false, IndexindResponse.INDEXING_ALREADY_BEGIN);
        }

        if (!siteEntities.isEmpty()
                && siteEntities.stream().
                filter(siteEntity -> siteEntity.getStatus().equals(Status.INDEXING)).toList().isEmpty()) {
            siteService.deleteAllSites();
        }

        addSites();
        final int countOfSites = siteService.getAllSites().size();

        try {

            siteService.getAllSites().forEach(site -> {
                        String path = checkLinkService.getPath(site.getUrl());
                Thread thread = new Thread(() -> {
                    PageProcessor task = new PageProcessor(scrubbingService, checkLinkService
                            , pageService, site.getUrl(), site);
                    tasks.add(task);
                    ForkJoinPool pool = new ForkJoinPool(cores / countOfSites);
                    listOfPools.add(pool);
                    pool.invoke(task);
                    pool.shutdown();
                    site.setStatus(Status.INDEXING);
                    site.setStatusTime(LocalDateTime.now());
                    siteService.updateSite(site);
                });
                threads.add(thread);
                thread.start();
                        checkLinkService.saveLink(new CheckLinkEntity(path, site));

                    }
            );

            return new IndexindResponse(true, null);
        } catch (Exception e) {
            siteService.getAllSites().forEach(site -> {
                site.setStatus(Status.FAILED);
                site.setStatusTime(LocalDateTime.now());
                site.setLastError(e.getLocalizedMessage());
                siteService.updateSite(site);
            });
            return new IndexindResponse(false, IndexindResponse.INDEXING_FAILED);
        }
    }

    @Override
    public IndexindResponse stopIndexingAllSites() {
        if (siteService.getAllSites().isEmpty()) {
            return new IndexindResponse(false, IndexindResponse.INDEXING_NOT_BEGIN);
        }
        if (!isIndexingStopped.get()) {
            isIndexingStopped.set(true);
        }
        log.info("Размер forkjoinpool " + listOfPools.get(0).getQueuedTaskCount());
        if (!listOfPools.isEmpty()) {
            listOfPools.forEach(ForkJoinPool::shutdown);
        }

        siteService.getAllSites().forEach(siteEntity -> {
            siteEntity.setStatus(Status.FAILED);
            siteEntity.setLastError("Индексация остановлена пользователем");
            siteService.saveSite(siteEntity);
        });
        log.info("Записали в БД Fail");


        return new IndexindResponse(true, null);
    }

    @Override
    public void indexing() {

    }

    @Override
    public boolean startIndexingPage() {
        return false;
    }

    private void addSites() {
        List<SiteConfig> siteConfigs = sitesList.getSiteConfigs();
        for (SiteConfig siteConfig : siteConfigs) {
            SiteEntity siteEntity = new SiteEntity(Status.INDEXING, LocalDateTime.now(), null,
                    siteConfig.getUrl(), siteConfig.getName());
            siteEntity.setId(siteService.saveSite(siteEntity).getId());
        }
    }
}

