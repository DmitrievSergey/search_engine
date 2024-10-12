package searchengine.services.site;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesList;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class SiteServiceImpl implements SiteService<SiteEntity> {
    private final SiteRepository siteRepository;
    private final SitesList sitesList;

    public SiteServiceImpl(SiteRepository siteRepository, SitesList sitesList) {
        this.siteRepository = siteRepository;
        this.sitesList = sitesList;
    }

    @Override
    public void addAllSites() {
        for (SiteConfig siteConfig : sitesList.getSiteConfigs()) {
            SiteEntity siteEntity = new SiteEntity(Status.INDEXING, LocalDateTime.now(), null,
                    siteConfig.getUrl(), siteConfig.getName());
            siteEntity.setId(saveSite(siteEntity).getId());
        }
    }

    @Override
    public SiteEntity saveSite(SiteEntity site) {
        return siteRepository.save(site);
    }

    @Override
    public SiteEntity updateSite(SiteEntity siteEntity, Status status, String error) {
        siteEntity = findSiteByUrl(siteEntity.getUrl());
        siteEntity.setStatus(status);
        siteEntity.setLastError(error);
        siteEntity.setStatusTime(LocalDateTime.now());
        return siteRepository.save(siteEntity);
    }

    @Override
    public SiteEntity updateSite(SiteEntity siteEntity, Status status) {
        siteEntity = findSiteByUrl(siteEntity.getUrl());
        siteEntity.setStatus(status);
        siteEntity.setLastError(null);
        siteEntity.setStatusTime(LocalDateTime.now());
        return siteRepository.save(siteEntity);
    }

    @Override
    public List<SiteEntity> getAllSites() {
        return siteRepository.findAll();
    }

    @Override
    public SiteEntity findSiteByUrl(String url) {
        return siteRepository.findSiteByUrl(url);
    }

    @Override
    public void deleteAllSites() {
        siteRepository.deleteAll();
    }
}
