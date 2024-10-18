package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.LemmaEntity;
import searchengine.entity.SiteEntity;

import java.util.List;

public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    Boolean existsByLemmaAndSite(String lemma, SiteEntity site);

    Long countBySite(SiteEntity site);

    LemmaEntity findByLemmaAndSite(String lemma, SiteEntity site);

    List<LemmaEntity> findAllByLemma(String lemma);

    List<LemmaEntity> findAllByLemmaAndSite(String lemma, SiteEntity site);

    @Modifying
    @Transactional
    @Query("DELETE FROM LemmaEntity")
    void deleteAllLemmas();
}
