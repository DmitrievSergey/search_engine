package searchengine.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.SearchEntity;
@Repository
public interface SearchRepository extends JpaRepository<SearchEntity, Integer> {
    Page<SearchEntity> findAllByQuery(String query, Pageable pageable);

    Page<SearchEntity> findAllByQueryAndSite(String query, String site, Pageable pageable);

    boolean existsByQuery(String query);

    boolean existsByQueryAndSite(String query, String site);

    boolean existsBySiteAndUri(String site, String uri);

    Integer countByQuery(String query);

    Integer countByQueryAndSite(String query, String site);

    void deleteAllBySiteAndUri(String site, String uri);
}
