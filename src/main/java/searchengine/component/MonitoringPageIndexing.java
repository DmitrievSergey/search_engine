package searchengine.component;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.component.index.IndexService;
import searchengine.component.lemma.LemmaService;
import searchengine.component.page.PageService;
import searchengine.component.scrabbing.JsoupConnection;
import searchengine.component.site.SiteService;
import searchengine.config.SiteConfig;
import searchengine.dto.exception.CustomInterruptException;
import searchengine.dto.statistics.PageStatistic;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;


import java.net.URI;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class MonitoringPageIndexing implements MonitoringService, JsoupConnection {
    private static Logger logger = LoggerFactory.getLogger(MonitoringPageIndexing.class);

    private final String url;
    private final SearchService searchService;
    private final SiteService<SiteEntity> siteService;
    private final PageService<PageEntity> pageService;
    private final IndexService indexService;
    private final LemmaService lemmaService;
    private final SiteConfig siteConfig;

    public MonitoringPageIndexing(String url, SiteService<SiteEntity> siteService, PageService<PageEntity> pageService,
                                  IndexService indexService, LemmaService lemmaService, SiteConfig siteConfig,
                                  SearchService searchService) {
        this.url = url;
        this.siteService = siteService;
        this.pageService = pageService;
        this.indexService = indexService;
        this.lemmaService = lemmaService;
        this.siteConfig = siteConfig;
        this.searchService = searchService;

    }

    private void monitoringIndexind(String url) throws InterruptedException {
        SiteEntity site = siteService.findSiteByUrl(siteConfig.getUrl());
        if (site == null) {
            logger.info(" Добавляем данные по сайту {} ", siteConfig.getUrl());
            try {
                site = siteService.setIndexingStatus(siteConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
            IndexingService.isIndexingRunning.set(true);
            logger.info(" Добавили данные по сайту {} ", siteConfig.getUrl());
        } else {

            try {
                logger.info("Нашли сайт {} c id {}", site.getName(), site.getId());
                siteService.setIndexingStatus(site);
                logger.info("Выставили статус {}  сайту {} c id {}", site.getStatus(), site.getName(), site.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            IndexingService.isIndexingRunning.set(true);
            logger.info("Выставили статус индексации в {}", IndexingService.isIndexingRunning.get());
        }


        try {
            PageEntity page = pageService.checkLinkInDB(url, site);
            if(page != null) {
                logger.info("Нашли данные по странице {} сайта {} c id {}", page.getPath(), site.getName(), site.getId());
                logger.info("Удаляем данные по странице {} сайта {}", page.getPath(), site.getName());
                deletePageData(page);
                logger.info("Удалили данные по странице {} сайта {}", page.getPath(), site.getName());
            }

            logger.info("Забираем контент страницы {} сайта {}", page.getPath(), site.getName());
            PageStatistic pageStatistic = getPageData(url);
            String pagePath = new URI(pageStatistic.getUrl()).getPath();
            page = new PageEntity(site, pagePath, pageStatistic.getCode(),  pageStatistic.getContent());
            logger.info("Получили контент страницы " + pageStatistic);
            pageService.save(page);
            if (pageStatistic.getCode() >= 400) throw new CustomInterruptException(
                    CustomInterruptException.PAGE_UNREACHABLE,
                    pageStatistic.getCode(),
                    url
            );
            Map<Integer, Integer> lemmaIdsAndFrequency;
            try {
                logger.info("Формируем леммы по странице" + page.getPath());
                lemmaIdsAndFrequency = lemmaService.addPageLemmaToDb(page, site);
                logger.info("Добавили леммы по странице в бд" + page.getPath());
                logger.info("Формируем индексы по странице" + page.getPath());
                indexService.addPageIndexToDb(page, lemmaIdsAndFrequency);
                logger.info("Добавили индексы по странице в бд" + page.getPath());
                siteService.setIndexedStatus(site);
                logger.info("Завершили индексацию по странице {} сайта {} ", url, siteConfig.getName());
                ForkJoinPool.commonPool().shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        catch (CustomInterruptException e) {
            siteService.setFailedStatus(site, e.getMessage());
            Thread.currentThread().interrupt();
            throw new InterruptedException();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void deletePageData(PageEntity page) {
        searchService.deleteSearchData();
        List<Integer> lemmasId = indexService.getLemmasIdsByPage(page);
        indexService.deleteByPageEntity(page);
        lemmaService.deleteLemmasByIds(lemmasId);
        pageService.deletePage(page);

    }

    @SneakyThrows
    @Override
    public void run() {
        monitoringIndexind(url);
    }

    private PageStatistic getPageData(String url) {
        try {
            Document document = JsoupConnection.getConnect(url);
            String html = document.outerHtml();
            Connection.Response response = document.connection().response();
            int statusCode = response.statusCode();
            return new PageStatistic(url, html, statusCode);
        } catch (CustomInterruptException e) {
            throw new CustomInterruptException(CustomInterruptException.PAGE_UNREACHABLE, e.getStatusCode() , e.getUrl());
        }

    }
}
