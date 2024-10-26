package searchengine.component;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import searchengine.config.SiteConfig;
import searchengine.dto.exception.CustomInterruptException;
import searchengine.dto.exception.CustomStopIndexingException;
import searchengine.dto.statistics.IndexStatistic;
import searchengine.dto.statistics.LemmaStatistic;
import searchengine.dto.statistics.PageStatistic;
import searchengine.entity.*;
import searchengine.component.site.SiteService;
import searchengine.component.index.IndexService;
import searchengine.component.lemma.LemmaService;
import searchengine.services.indexing.IndexingService;
import searchengine.component.page.PageService;
import searchengine.component.scrabbing.LinkProcessor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class MonitoringSiteIndexing implements MonitoringService {
    private static Logger logger = LoggerFactory.getLogger(MonitoringSiteIndexing.class);

    private final SiteService<SiteEntity> siteService;
    private final PageService<PageEntity> pageService;
    private final IndexService indexService;
    private final LemmaService lemmaService;
    private final SiteConfig siteConfig;


    ForkJoinPool forkJoinPool;

    public MonitoringSiteIndexing(SiteService<SiteEntity> siteService, PageService<PageEntity> pageService,
                                  IndexService indexService, LemmaService lemmaService, SiteConfig siteConfig) {
        this.siteService = siteService;
        this.pageService = pageService;
        this.indexService = indexService;
        this.lemmaService = lemmaService;
        this.siteConfig = siteConfig;
    }

     private List<PageStatistic> getPageList(String url) throws InterruptedException {

        try {
            List<PageStatistic> pageStatisticVector = new Vector<>();
            List<String> urlList = new Vector<>();
            forkJoinPool = new ForkJoinPool(IndexingService.CORES);
            List<PageStatistic> pages = forkJoinPool.invoke(new LinkProcessor(url, pageStatisticVector, urlList, siteConfig));
            return new CopyOnWriteArrayList<>(pages);
        }
        catch (CustomInterruptException e) {
            forkJoinPool.shutdownNow();
            throw new CustomInterruptException(e.getMessage(), e.getStatusCode(), e.getUrl());
        }
        catch (CustomStopIndexingException e) {
            throw new CustomStopIndexingException(e.getMessage(), e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void monitoringIndexind(SiteConfig siteConfig) throws InterruptedException {
        if (siteService.findSiteByUrl(siteConfig.getUrl()) != null) {
            logger.debug("Удаляем данные по сайту {} ", siteConfig.getUrl());
            deleteSiteData(siteConfig);
            logger.debug("Данные по сайту {} удалены ", siteConfig.getUrl());
        }
        logger.info(" Добавляем данные по сайту {} ", siteConfig.getUrl());
        SiteEntity site = siteService.addSiteData(siteConfig);
        logger.info(" Добавили данные по сайту {} ", siteConfig.getUrl());
        try {
            List<PageStatistic> pageStatisticList = getPageList(site.getUrl());
            logger.debug("Получили список ссылок " + pageStatisticList);
            pageService.addToDB(pageStatisticList, site);
            getSiteLemmas(site);
            getSiteIndex(site);
            siteService.setIndexedStatus(site);
            logger.info("Завершили индексацию по сайту {} ", siteConfig.getName());
            pageStatisticList.clear();
            forkJoinPool.shutdownNow();
            ForkJoinPool.commonPool().shutdown();
            logger.info("Индексация завершилась {}", IndexingService.isIndexingRunning.get());
        } catch(CustomStopIndexingException | CustomInterruptException e) {
            logger.info("Мессага пр ексепшн " + e.getMessage());
            siteService.setFailedStatus(site, e.getMessage());
            forkJoinPool.shutdownNow();
            throw new CustomStopIndexingException(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    protected void getSiteIndex(SiteEntity site) throws InterruptedException {
        if(IndexingService.isIndexingStopped.get()) {
            throw new CustomStopIndexingException("Индексация остановлена пользователем.", new InterruptedException());
        }
        try {
                logger.info("getSiteIndex - siteName {} Поток прерван {}", site.getName(), Thread.currentThread().isInterrupted());
//                List<IndexStatistic> indexStatisticList = new CopyOnWriteArrayList<>();
                List<IndexSearchEntity> indexSearchEntityList = new CopyOnWriteArrayList<>();
                Map<String, LemmaEntity> lemmaEntityMap = lemmaService.getAllBySite(site);
                List<PageEntity> pageEntityList = pageService.getPagesBySite(site);

                for (PageEntity page : pageEntityList) {
                    for (Map.Entry<String, Integer> entry : lemmaService.getPageLemmas(page).entrySet()) {
                        indexSearchEntityList.add(new IndexSearchEntity(page,
                                lemmaEntityMap.get(entry.getKey()),
                                entry.getValue()));
//                        indexStatisticList.add(new IndexStatistic(page.getId(),
//                                lemmaEntityMap.get(entry.getKey()).getId(),
//                                entry.getValue().floatValue()));
                    }
                }
                indexService.saveAll(indexSearchEntityList);
                logger.info("Size index" + indexSearchEntityList.size());
            } catch (CustomStopIndexingException e) {
                throw new CustomStopIndexingException(e.getMessage(), e);
            }
    }

    protected void getSiteLemmas(SiteEntity site) throws InterruptedException {
        try {
            logger.info("getSiteLemmas - siteName {} Поток прерван {}", site.getName(), Thread.currentThread().isInterrupted());
            List<LemmaStatistic> lemmaStatisticList = lemmaService.getLemmaDTOStatistic(site);
            logger.info("Получили лист лемм {} ", lemmaStatisticList.size());
            List<LemmaEntity> lemmaList = new CopyOnWriteArrayList<>();
            lemmaStatisticList.parallelStream().forEach(lemmaStatistic -> {
                lemmaList.add(new LemmaEntity(site, lemmaStatistic.getLemma(),
                        lemmaStatistic.getFrequency()));
            });
            try {
                lemmaService.saveAll(lemmaList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (CustomStopIndexingException e) {
            throw new CustomStopIndexingException(e.getMessage(), e);
        }

    }



    protected void deleteSiteData(SiteConfig siteConfig) {
        SiteEntity site = siteService.findSiteByUrl(siteConfig.getUrl());
        siteService.delete(site);
    }

    @SneakyThrows
    @Override
    public void run() {
        monitoringIndexind(siteConfig);
    }

}
