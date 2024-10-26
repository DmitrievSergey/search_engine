package searchengine.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.PageEntity;
import searchengine.entity.SearchEntity;

import java.util.List;

public interface SearchRepository extends JpaRepository<SearchEntity, Integer> {
    Page<SearchEntity> findAllByQuery(String query, Pageable pageable);

    Page<SearchEntity> findAllByQueryAndSite(String query, String site, Pageable pageable);

    boolean existsByQuery(String query);

    boolean existsByQueryAndSite(String query, String site);

    boolean existsBySiteAndUri(String site, String uri);

    Integer countByQuery(String query);

    Integer countByQueryAndSite(String query, String site);

    @Query("select se from SearchEntity se where se.query=?1")
    List<SearchEntity> findAllByQuery(String query);

    @Query("select se from SearchEntity se where se.query=?1 and se.site=?2")
    List<SearchEntity> findAllByQueryAndSite(String query, String site);

    void deleteAllBySiteAndUri(String site, String uri);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE `search` ", nativeQuery = true)
    void clearSearch();
}
