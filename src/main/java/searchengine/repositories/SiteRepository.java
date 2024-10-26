package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    @Query("select s from SiteEntity s where s.url = ?1")
    SiteEntity findSiteByUrl(String path);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE site", nativeQuery = true)
    void deleteAllSites();
}
