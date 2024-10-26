package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    List<PageEntity> findAllBySite(SiteEntity site);

    @Query("select p from PageEntity p where p.site.id = ?1 and p.responseCode < 400")
    List<PageEntity> findAllBySiteIdAndResponseCode(int siteId);

    @Query("select p from PageEntity p where p.site = ?1 and p.path = ?2")
    PageEntity existsByPathAndAndSite(SiteEntity site, String path);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE page", nativeQuery = true)
    void deleteAllPages();


}
