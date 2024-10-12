package searchengine.services.scrabbing;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.indexing.IndexingService;
import searchengine.services.jsoup.JsoupService;
import searchengine.services.page.PageService;


import java.util.*;
import java.util.concurrent.*;

import static java.lang.Integer.compare;

@Slf4j
@Getter
@Setter

public class PageProcessor extends RecursiveTask<List<String>> implements Comparable<PageProcessor> {
    private final JsoupService jsoupService;
    private String url;
    private final SiteEntity site;

    public PageProcessor(JsoupService jsoupService, String url, SiteEntity site) {
        this.jsoupService = jsoupService;
        this.site = site;
        this.url = url.toLowerCase();
    }

    @Override
    public String toString() {
        return "PageProcessor{" +
                "url='" + url + '\'' +
                ", site=" + site +
                '}';
    }



    @Override
    protected List<String> compute() {
        List<String> resultList;
        try {
            Set<String> childLinks = jsoupService.getUrlsSetFromUrl(url, site);
            Set<PageProcessor> taskList = new TreeSet<>();
            resultList = new ArrayList<>();
            for (String child : childLinks) {
                PageProcessor task = new PageProcessor(jsoupService, child, site);
                taskList.add(task);
            }

            ForkJoinTask.invokeAll(taskList);
            for (PageProcessor task : taskList) {
                resultList.addAll(task.join());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return resultList;

    }

    @Override
    public int compareTo(PageProcessor o) {
        int x = compare(o.getSite().getId(), this.getSite().getId());
        int y = this.url.compareTo(o.url);

        return Integer.compare(x, y);
    }
}
