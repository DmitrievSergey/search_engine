package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;


public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query("select p from PageEntity p where p.path = ?2 and p.site.id = ?1")
    PageEntity findBySiteIdAndPath(int siteId, String path);

    Long countBySite(SiteEntity site);

    @Modifying
    @Transactional
    @Query("DELETE FROM PageEntity ")
    void deleteAllPages();

    @Transactional
    void deleteByPathAndSite(String path, SiteEntity site);

}
