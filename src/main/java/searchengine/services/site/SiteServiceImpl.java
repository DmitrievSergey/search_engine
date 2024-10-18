package searchengine.services.site;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConfig;
import searchengine.config.SitesListConfig;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class SiteServiceImpl implements SiteService<SiteEntity> {
    private final SiteRepository siteRepository;
    private final SitesListConfig sitesList;

    public SiteServiceImpl(SiteRepository siteRepository, SitesListConfig sitesList) {
        this.siteRepository = siteRepository;
        this.sitesList = sitesList;
    }

    @Override
    public void addAllSites() {
        for (SiteConfig site : sitesList.getSites()) {
            SiteEntity siteEntity = SiteEntity
                    .builder()
                    .status(Status.INDEXING)
                    .statusTime(LocalDateTime.now())
                    .lastError(null)
                    .name(site.getName())
                    .url(site.getUrl())
                    .build();
            siteRepository.save(siteEntity);
        }
    }

    @Override
    public SiteEntity saveSite(SiteEntity site) {
        site = SiteEntity.builder()
                .statusTime(LocalDateTime.now())
                .build();
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

    public boolean existInDB(String url) {
        boolean exists = false;
        List<SiteEntity> listOfSites = getAllSites();
        if(listOfSites.isEmpty()) return exists;
        for(SiteEntity site : listOfSites) {
            if(url.contains(site.getUrl())) exists = true;
        }
        return exists;
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
    public SiteEntity findSiteById(int id) {
        return siteRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteAllSites() {
        siteRepository.deleteAll();
    }

    @Override
    public Long getSitesCount() {
        return siteRepository.count();
    }
}
