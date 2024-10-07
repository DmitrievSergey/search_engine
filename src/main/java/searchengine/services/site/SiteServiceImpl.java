package searchengine.services.site;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import searchengine.entity.SiteEntity;
import searchengine.repositories.SiteRepository;

import java.util.List;

@AllArgsConstructor
@Service
public class SiteServiceImpl implements SiteService<SiteEntity> {
    SiteRepository siteRepository;

    @Override
    public SiteEntity saveSite(SiteEntity site) {
        return siteRepository.save(site);
    }

    @Override
    public SiteEntity updateSite(SiteEntity siteEntity) {
        return siteRepository.save(siteEntity);
    }

    @Override
    public List<SiteEntity> getAllSites() {
        return siteRepository.findAll();
    }

    @Override
    public void deleteAllSites() {
        siteRepository.deleteAll();
    }
}
