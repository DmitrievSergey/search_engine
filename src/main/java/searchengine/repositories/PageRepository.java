package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.entity.PageEntity;


public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query("select p from page p where p.path = ?2 and p.site.id = ?1")
    PageEntity findBySiteIdAndPath(int siteId, String path);

}
