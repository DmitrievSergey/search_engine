package searchengine.services.jsoup;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.JSOUPSettings;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.indexing.IndexingServiceImpl;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
@Service
public class JsoupServiceImpl implements JsoupService{
    private final JSOUPSettings jsoupSettings;
    private final CheckLinkService<CheckLinkEntity> checkLinkService;
    private ConcurrentSkipListSet<String> siteLinkSet = new ConcurrentSkipListSet<String>();

    public JsoupServiceImpl(CheckLinkService<CheckLinkEntity> checkLinkService, JSOUPSettings jsoupSettings) {
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
                if(! checkLinkService.isValid(link, url, site)) continue;
                if(siteLinkSet.contains(link)) continue;
                siteLinkSet.add(link);
                pageUrls.add(link);

            }

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch(UnknownHostException e) {
            e.printStackTrace();
            log.info("Поймали на урле = " + url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Size of listSiteUrls " + siteLinkSet.size());
        return pageUrls;
    }

}
