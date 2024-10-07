package searchengine.services.scrabbing;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.entity.CheckLinkEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.services.checklink.CheckLinkService;
import searchengine.services.indexing.IndexingService;
import searchengine.services.page.PageService;


import java.util.*;
import java.util.concurrent.*;

import static java.lang.Integer.compare;

@Slf4j
@Getter
@Setter

public class PageProcessor extends RecursiveAction implements Comparable<PageProcessor> {
    private final ScrubbingService scrubbingService;
    private final CheckLinkService<CheckLinkEntity> checkLinkService;
    private final PageService<PageEntity> pageService;
    private String url;
    private final SiteEntity site;

    public PageProcessor(ScrubbingService scrubbingService, CheckLinkService<CheckLinkEntity> checkLinkService,
                         PageService<PageEntity> pageService, String url, SiteEntity site) {
        this.scrubbingService = scrubbingService;
        this.checkLinkService = checkLinkService;
        this.pageService = pageService;
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
    protected void compute() {
    try {
        Set<String> childLinks = scrubbingService.getPageLinks(url, site);
        Set<PageProcessor> taskList = new TreeSet<>();

        for (String child : childLinks) {
            PageProcessor task = new PageProcessor(scrubbingService, checkLinkService, pageService, child, site);
            taskList.add(task);
        }

        ForkJoinTask.invokeAll(taskList);
        for (PageProcessor task : taskList) {
            task.join();
        }

    } catch (Exception e){
        e.printStackTrace();

    }

    }

    @Override
    public int compareTo(PageProcessor o) {
        int x = compare(o.getSite().getId(), this.getSite().getId());
        int y = this.url.compareTo(o.url);

        return Integer.compare(x, y);
    }
}
