package searchengine.services.scrabbing;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.indexing.IndexingServiceImpl;
import searchengine.services.jsoup.JsoupService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private ConcurrentSkipListSet<String> siteLinkSet = new ConcurrentSkipListSet<String>();
    private static final String workDir = new File("").getAbsolutePath() + File.separator;
    private static AtomicInteger count = new AtomicInteger(0);

    private final CheckLinkService<CheckLinkEntity> checkLinkService;
    private final JsoupService jsoupService;

    public FileServiceImpl(JsoupService jsoupService, CheckLinkService<CheckLinkEntity> checkLinkService) {
        this.checkLinkService = checkLinkService;
        this.jsoupService = jsoupService;
    }


    private void writSortedArrayToFile(Set<String> first, SiteEntity site, AtomicInteger count) {
        String resultFile = workDir + "src/main/resources/fileLinks/" + site.getName() + count + ".txt";

        try {
            FileWriter writer = new FileWriter(resultFile, true);
            for(String string: first) {
                writer.write(string + "\n");
            }

            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public Set<String> writeLinksToFile(String url, SiteEntity site) {
        long start = System.currentTimeMillis();
        Set<String> pageUrls = new TreeSet<>(jsoupService.getUrlsSetFromUrl(url, site));
        log.info("Размер siteLinkSet" + siteLinkSet.size());
        siteLinkSet.addAll(pageUrls);
        return pageUrls;
    }

}
