package searchengine.services.scrabbing;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import searchengine.entity.SiteEntity;
import searchengine.services.jsoup.JsoupService;


import java.util.*;
import java.util.concurrent.*;

import static java.lang.Integer.compare;

@Slf4j
@Getter
@Setter

public class LinkProcessor extends RecursiveTask<Set<LinkProcessor>> implements Comparable<LinkProcessor> {
    private final JsoupService jsoupService;
    private String url;
    private final SiteEntity site;

    public LinkProcessor(JsoupService jsoupService, String url, SiteEntity site) {
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
    protected Set<LinkProcessor> compute() {
        Set<LinkProcessor> taskList;

        try {
            Set<String> childLinks = jsoupService.getUrlsSetFromUrl(url, site);
            taskList = new TreeSet<>();
            for (String child : childLinks) {
                LinkProcessor task = new LinkProcessor(jsoupService, child, site);
                taskList.add(task);

            }
            ForkJoinTask.invokeAll(taskList);

            for (LinkProcessor task : taskList) {
                task.join();
            }


        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }


        return taskList;

    }

    @Override
    public int compareTo(LinkProcessor o) {
        int x = compare(o.getSite().getId(), this.getSite().getId());
        int y = this.url.compareTo(o.url);

        return Integer.compare(x, y);
    }
}
