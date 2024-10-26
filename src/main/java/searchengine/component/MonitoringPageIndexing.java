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


import java.net.URI;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class MonitoringPageIndexing implements MonitoringService, JsoupConnection {
    private static Logger logger = LoggerFactory.getLogger(MonitoringPageIndexing.class);

    private final String url;
    private final SiteService<SiteEntity> siteService;
    private final PageService<PageEntity> pageService;
    private final IndexService indexService;
    private final LemmaService lemmaService;
    private final SiteConfig siteConfig;

    public MonitoringPageIndexing(String url, SiteService<SiteEntity> siteService, PageService<PageEntity> pageService,
                                  IndexService indexService, LemmaService lemmaService, SiteConfig siteConfig) {
        this.url = url;
        this.siteService = siteService;
        this.pageService = pageService;
        this.indexService = indexService;
        this.lemmaService = lemmaService;
        this.siteConfig = siteConfig;

    }

    private void monitoringIndexind(String url) throws InterruptedException {
        SiteEntity site = siteService.findSiteByUrl(siteConfig.getUrl());
        if (site == null) {
            logger.info(" Добавляем данные по сайту {} ", siteConfig.getUrl());
            site = siteService.addSiteData(siteConfig);
            logger.info(" Добавили данные по сайту {} ", siteConfig.getUrl());
        }

        try {
            PageEntity page = pageService.checkLinkInDB(url, site);
            if(page != null) deletePageData(page);
            PageStatistic pageStatistic = getPageData(url);
            String pagePath = new URI(pageStatistic.getUrl()).getPath();
            page = new PageEntity(site, pagePath, pageStatistic.getCode(),  pageStatistic.getContent());
            logger.debug("Получили контент страницы " + pageStatistic);
            pageService.save(page);
            if (pageStatistic.getCode() >= 400) throw new CustomInterruptException(
                    CustomInterruptException.PAGE_UNREACHABLE,
                    pageStatistic.getCode(),
                    url
            );
            Map<Integer, Integer> lemmaIdsAndFrequency;
            try {
                lemmaIdsAndFrequency = lemmaService.addPageLemmaToDb(page, site);
                indexService.addPageIndexToDb(page, lemmaIdsAndFrequency);
                siteService.setIndexedStatus(site);
                logger.info("Завершили индексацию по странице {} сайта {} ", url, siteConfig.getName());
                logger.info("Индексация завершилась {}", IndexingService.isIndexingRunning.get());
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
