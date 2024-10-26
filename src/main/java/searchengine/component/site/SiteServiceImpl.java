package searchengine.component.site;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.SiteConfig;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService<SiteEntity>{
    private final SiteRepository siteRepository;
    @Override
    public SiteEntity findSiteByUrl(String url) {
        return siteRepository.findSiteByUrl(url);
    }

    @Override
    public SiteEntity save(SiteEntity site) {
        site.setStatusTime(LocalDateTime.now());
        siteRepository.flush();
        return siteRepository.save(site);
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
    public SiteEntity addSiteData(SiteConfig siteConfig) {
        SiteEntity site = new SiteEntity(Status.INDEXING, LocalDateTime.now(), null, siteConfig.getUrl(), siteConfig.getName());
        return save(site);
    }

}
