package searchengine.component.site;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import searchengine.component.MonitoringPageIndexing;
import searchengine.config.SiteConfig;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService<SiteEntity>{
    private static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);
    private final SiteRepository siteRepository;
    @Override
    public SiteEntity findSiteByUrl(String url) {
        return siteRepository.findSiteByUrl(url);
    }

    @Override
    public SiteEntity save(SiteEntity site) {
        site.setStatusTime(LocalDateTime.now());
        logger.info("Begin flush ");
        siteRepository.flush();
        logger.info("End flush ");
        logger.info("Begin save site ");
        SiteEntity siteEntity = null;
        try {
            siteEntity = siteRepository.save(site);
            logger.info("End save site ");
            return siteEntity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return siteEntity;

    }

    @Override
    public void delete(SiteEntity site) {
        siteRepository.delete(site);
    }

    @Override
    public SiteEntity setFailedStatus(SiteEntity site, String error) {
        site.setStatus(Status.FAILED);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(error);
        return save(site);
    }

    @Override
    public SiteEntity setIndexedStatus(SiteEntity site) {
        site.setStatus(Status.INDEXED);
        return save(site);
    }

    @Override
    public void deleteAll() {
        siteRepository.deleteAll();
    }

    @Override
    public SiteEntity findSiteBySiteId(Integer id) {
        return siteRepository.findById(id).orElseThrow();
    }
    @Override
    public SiteEntity setIndexingStatus(SiteConfig siteConfig) {
        SiteEntity site = new SiteEntity(Status.INDEXING, LocalDateTime.now(), null, siteConfig.getUrl(), siteConfig.getName());
        return save(site);
    }

    @Override
    public SiteEntity setIndexingStatus(SiteEntity site) {
        site.setStatus(Status.INDEXING);
        return save(site);
    }

    @Override
    public List<SiteEntity> findSitesWithStatusIndexed() {
        return siteRepository.findSiteByStatusIndexed();
    }

}
