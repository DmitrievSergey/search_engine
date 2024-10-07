package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import searchengine.entity.CheckLinkEntity;

public interface CheckLinkRepository extends JpaRepository<CheckLinkEntity, Integer>{
    @Query("select l from CheckLinkEntity l where l.site.id=?2 and l.path=?1")
    CheckLinkEntity existsByPathEqualsAndSiteId(String path, int siteId);
}
