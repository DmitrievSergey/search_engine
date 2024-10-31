package searchengine.component.page;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import searchengine.dto.statistics.PageStatistic;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.repositories.PageRepository;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Component
public class PageServiceImpl implements PageService<PageEntity> {
    private static Logger logger = LoggerFactory.getLogger(PageServiceImpl.class);
    private PageRepository pageRepository;

    public PageServiceImpl(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Override
    public List<PageEntity> saveAll(List<PageEntity> pageList) {
        pageRepository.flush();
        return pageRepository.saveAll(pageList);
    }

    @Override
    public List<PageEntity> getPagesBySite(SiteEntity site) {
        return pageRepository.findAllBySiteIdAndResponseCode(site.getId());
    }

    @Override
    public void deleteAllPages() {
        pageRepository.deleteAllPages();
    }


    @Override
    public PageEntity checkLinkInDB(String url, SiteEntity site) throws URISyntaxException, MalformedURLException {
        String pageUrlPath = new URI(url).toURL().getPath();
        PageEntity page = pageRepository.existsByPathAndAndSite(site, pageUrlPath);
        return page;
    }

    @Override
    public void deletePage(PageEntity page) {
        pageRepository.delete(page);
    }
    @Override
    public void addToDB(List<PageStatistic> pageStatisticList, SiteEntity site) {
        if (!Thread.currentThread().isInterrupted()) {
            logger.info("addToDB - siteName {} ", site.getName());
            List<PageEntity> pageList = new CopyOnWriteArrayList<>();

            pageStatisticList.parallelStream().forEach(pageStatistic -> {
                try {
                    pageList.add(new PageEntity(site,
                            getPathFromUrl(pageStatistic.getUrl()),
                            pageStatistic.getCode(),
                            pageStatistic.getContent()));
                } catch (URISyntaxException | MalformedURLException e) {
                    e.printStackTrace();
                }
            });
            saveAll(pageList);

        }
    }

    @Override
    public void save(PageEntity page) {
        pageRepository.flush();
        pageRepository.save(page);
    }

    @Override
    public PageEntity getPageById(Integer p) {

        return pageRepository.findById(p).orElseThrow();
    }

    private String getPathFromUrl(String url) throws URISyntaxException, MalformedURLException {
        return new URI(url).toURL().getPath();
    }
}
