package searchengine.services.scrabbing;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.JSOUPSettings;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.indexing.IndexingService;
import searchengine.services.indexing.IndexingServiceImpl;
import searchengine.services.page.PageService;


import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrubbingServiceImpl implements ScrubbingService {
    private final JSOUPSettings jsoupSettings;
    private final PageService<PageEntity> pageService;
    private final CheckLinkService<CheckLinkEntity> checkLinkService;

    public ScrubbingServiceImpl(JSOUPSettings jsoupSettings, PageService<PageEntity> pageService, CheckLinkService<CheckLinkEntity> checkLinkService) {
        this.jsoupSettings = jsoupSettings;
        this.pageService = pageService;
        this.checkLinkService = checkLinkService;
    }


    private Connection connectToPage(String url) throws IOException {
        Random rand = new Random();
        String userAgent = jsoupSettings.getUserAgents().get(rand.nextInt(jsoupSettings.getUserAgents().size() - 1));

        return Jsoup.connect(url)
                .timeout(jsoupSettings.getTimeout())
                .followRedirects(jsoupSettings.isFollowRedirect())
                .ignoreHttpErrors(jsoupSettings.isIgnoreHTTPErrors())
                .userAgent(userAgent)
                .referrer(jsoupSettings.getReferrer());
    }

    @Override
    public synchronized Set<String> getPageLinks(String url, SiteEntity site) {
        long start = System.currentTimeMillis();
        Connection connection = null;
        Document document = null;
        Set<String> pageUrls = new HashSet<>();
        if (IndexingServiceImpl.isIndexingStopped.get()) {
            log.info("Индексация останавливается " + IndexingServiceImpl.isIndexingStopped.get());
            log.info("размер pageUrls " + pageUrls.size());
            return pageUrls;
        }

        try {
            connection = connectToPage(String.valueOf(checkLinkService.getUrl(url)));
            Thread.sleep(new Random().nextInt(jsoupSettings.getIntervals().size() - 1));
            document = connection.get();
            if (document == null) {
                return pageUrls;
            }
            pageService.saveSitePage(new PageEntity(site, checkLinkService.getPath(url)
                    , document.outerHtml(), connection.response().statusCode()));
            List<CheckLinkEntity> list = new ArrayList<>();
            Elements elements = document.select("a[href^=/]");
            for (Element element : elements) {
                String link = element.absUrl("href");
                if (!checkLinkService.isValid(link, site)) continue;
                String path = checkLinkService.getPath(link);
                CheckLinkEntity checkLink = new CheckLinkEntity(path, site);
                if (list.contains(checkLink)) continue;
                if (checkLinkService.isLinkExist(checkLink)) continue;

                pageUrls.add(link);
                list.add(checkLink);

            }
            checkLinkService.saveLinks(list);
            log.info(" Время выполнения getPageLinks " + (System.currentTimeMillis() - start));

        } catch (InterruptedException | IOException exception) {
            exception.printStackTrace();
            pageService.saveSitePage(new PageEntity(site, checkLinkService.getPath(url)
                    , document.outerHtml(), connection.response().statusCode()));
        }

        return pageUrls;
    }
}
