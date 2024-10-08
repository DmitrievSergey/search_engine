package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    @Query("select s from SiteEntity s where s.url = ?1")
    SiteEntity findSiteByUrl(String path);
}
