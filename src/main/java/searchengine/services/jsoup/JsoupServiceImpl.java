package searchengine.services.jsoup;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.JSOUPSettings;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.index.IndexService;
import searchengine.services.indexing.IndexingServiceImpl;
import searchengine.services.lemma.LemmaService;
import searchengine.services.page.PageService;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
@Service
public class JsoupServiceImpl implements JsoupService {
    private final JSOUPSettings jsoupSettings;
    private final CheckLinkService checkLinkService;
    private final PageService<PageEntity> pageService;
    private final IndexService indexService;
    private final LemmaService lemmaService;


    public JsoupServiceImpl(IndexService indexService, LemmaService lemmaService, PageService<PageEntity> pageService
            , CheckLinkService checkLinkService, JSOUPSettings jsoupSettings) {
        this.indexService = indexService;
        this.lemmaService = lemmaService;
        this.pageService = pageService;
        this.checkLinkService = checkLinkService;
        this.jsoupSettings = jsoupSettings;

    }


    private Connection connectToPage(String url) {
        Random rand = new Random();
        String userAgent = jsoupSettings.getUserAgents().get(rand.nextInt(jsoupSettings.getUserAgents().size() - 1));
        try {
            Thread.sleep(new Random().nextInt(jsoupSettings.getIntervals().size()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Jsoup.connect(url)
                .timeout(jsoupSettings.getTimeout())
                .followRedirects(jsoupSettings.isFollowRedirect())
                .ignoreHttpErrors(jsoupSettings.isIgnoreHTTPErrors())
                .userAgent(userAgent)
                .referrer(jsoupSettings.getReferrer());
    }

    @Override
    public Set<String> getUrlsSetFromUrl(String url, SiteEntity site) {
        Connection connection;
        Document document = null;
        Set<String> pageUrls = new TreeSet<>();
        try {
            connection = connectToPage(url);
            document = connection.get();
            if (document == null || IndexingServiceImpl.isIndexingStopped.get()) {
                return Collections.emptySet();
            }
            Elements elements = document.select("a[href]:not([href^=tel]" +
                    ", [href~=#],[href^=javascript],[href~=,],[href^=mailto],[img])");

            for (Element element : elements) {
                String link = element.absUrl("href");
                if (!checkLinkService.isValid(link, url, site)) continue;
                if (siteLinkSet.contains(link)) continue;
                siteLinkSet.add(link);
                URL pageUrl = checkLinkService.getUrl(link);


                PageEntity page = PageEntity.builder()
                        .site(site)
                        .path(pageUrl.getPath())
                        .content(document.outerHtml())
                        .responseCode(connection.response().statusCode())
                        .build();
                pageService.saveSitePage(page);


                indexService.createIndexes(lemmaService.getLemmas(document, site),
                        page);
                pageUrls.add(link);
            }

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            log.info("Поймали на урле = " + url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return pageUrls;
    }
    @Override
    public void checkAndAddToIndexPage(String url, SiteEntity site) {
        Connection connection;
        Document document = null;
        try {
            connection = connectToPage(url);
            document = connection.get();
            if (document == null) {
                return;
            }
            if (!checkLinkService.isValid(url, site.getUrl(), site)) return;
            if (siteLinkSet.contains(url)) return;
            siteLinkSet.add(url);
            URL pageUrl = checkLinkService.getUrl(url);


            PageEntity page = PageEntity.builder()
                    .site(site)
                    .path(pageUrl.getPath())
                    .content(document.outerHtml())
                    .responseCode(connection.response().statusCode())
                    .build();
            pageService.saveSitePage(page);


            indexService.createIndexes(lemmaService.getLemmas(document, site),
                    page);

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            log.info("Поймали на урле = " + url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (IndexingServiceImpl.isIndexingStopped.get()) {
            return;
        }
    }

}
