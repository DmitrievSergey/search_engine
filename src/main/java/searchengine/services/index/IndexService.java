package searchengine.services.index;

import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;

import java.util.List;
import java.util.Map;

public interface IndexService {

    void createIndexes(Map<LemmaEntity, Integer> lemmaIntegerMap, PageEntity page);

    void deleteAllIndices();

    void deleteAllByEntities(List<IndexEntity> indices);

    List<IndexEntity> findAllByPage(PageEntity page);

    List<IndexEntity> findAllByLemma(LemmaEntity lemma);
}
