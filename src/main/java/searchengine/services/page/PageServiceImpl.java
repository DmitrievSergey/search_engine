package searchengine.services.page;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteConfig;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.repositories.PageRepository;

import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class PageServiceImpl implements PageService<PageEntity> {
    PageRepository pageRepository;

    @Override
    public PageEntity saveSitePage(PageEntity page) throws SQLException {

       return pageRepository.save(page);
    }

    @Override
    public Long getCount() {
        return pageRepository.count();
    }

    @Override
    public Long getCountBySite(SiteEntity site) {
        return pageRepository.countBySite(site);
    }

    public void updateSitePage(PageEntity page) {
        pageRepository.save(page);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveListOfSitePage(List<PageEntity> pageEntities) {
        pageRepository.saveAll(pageEntities);
    }

    @Override
    public void deleteAllSitePage(SiteConfig siteConfig) {

    }

    @Override
    public synchronized boolean getAllBySiteIdAndPath(PageEntity pageEntity) {
        PageEntity page = pageRepository.findBySiteIdAndPath(pageEntity.getSite().getId(), pageEntity.getPath());
        return page == null;
    }

    @Override
    public synchronized boolean getAllBySiteIdAndPath(int siteId, String path) {
        PageEntity page = pageRepository.findBySiteIdAndPath(siteId, path);
        return page == null;
    }
}
