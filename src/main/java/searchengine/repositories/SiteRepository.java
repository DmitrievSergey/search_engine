package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.entity.SiteEntity;

public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

}
