package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query(value = "select i.page_id from `index` i \n" +
            "where lemma_id in (:values ) \n" +
            "group by i.page_id\n" +
            "having COUNT(i.page_id) > 1 \n" +
            "order by i.page_id desc", nativeQuery = true)
    List<Integer> getPageIdsWithQueryLemmas(@Param("values") List<Integer> values);

    @Query(value ="select i.* from `index` i \n" +
            "where i.page_id in (:values)" ,nativeQuery = true)
    List<IndexSearchEntity> getIndexesByPageIds(@Param("values") List<Integer> values);
    @Query("select i from IndexSearchEntity i where i.page.id = ?1 and i.lemma.id = ?2")
    IndexSearchEntity getIndexByPageIdAndLemmaId(int pageId, int lemmaId);
}
