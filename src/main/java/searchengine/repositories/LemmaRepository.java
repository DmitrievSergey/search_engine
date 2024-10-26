package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.LemmaEntity;
import searchengine.entity.SiteEntity;

import java.util.List;
import java.util.Map;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {

    @Query("select l from LemmaEntity l where l.lemma = ?1 and l.site.id = ?2")
    LemmaEntity findLemmaEntityByLemmaAndSiteId(String lemma, int siteId);

    @Query("select l from LemmaEntity l where l.site.id = ?1")
    List<LemmaEntity> findLemmaEntityBySite(int siteId);

    Long countBySite(SiteEntity site);

    List<LemmaEntity> findAllByLemma(String lemma);


    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE lemma", nativeQuery = true)
    void deleteAllLemmas();

    @Query("select l from LemmaEntity l where l.id = ?1")
    LemmaEntity findById(Integer i);
}
