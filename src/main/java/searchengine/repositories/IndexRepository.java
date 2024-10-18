package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;

import java.util.List;

public interface IndexRepository extends JpaRepository<IndexEntity, Long> {
    List<IndexEntity> findAllByPage(PageEntity page);

    List<IndexEntity> findAllByLemma(LemmaEntity lemma);

    @Modifying
    @Transactional
    @Query("DELETE FROM IndexEntity")
    void deleteAllIndices();
}
