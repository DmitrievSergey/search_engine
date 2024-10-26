package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.entity.IndexSearchEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;

import java.util.List;
@Repository
public interface IndexRepository extends JpaRepository<IndexSearchEntity, Long> {
    @Query("select i.lemma.id from IndexSearchEntity i where i.page = ?1")
    List<Integer> findLemmasIdByPage(PageEntity page);

    List<IndexSearchEntity> findAllByPage(PageEntity page);

    List<IndexSearchEntity> findAllByLemma(LemmaEntity lemma);
    @Modifying
    @Transactional
    @Query("DELETE FROM IndexSearchEntity")
    void deleteAllIndices();

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE `index`", nativeQuery = true)
    void deleteAllIndexes();
}
