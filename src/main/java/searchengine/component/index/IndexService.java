package searchengine.component.index;

import searchengine.entity.IndexSearchEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;

import java.util.List;
import java.util.Map;

public interface IndexService {

    List<IndexSearchEntity> getIndexSearchesByLemmas(List<LemmaEntity> lemmas);

    void deleteByPageEntity(PageEntity page);

    void deleteAllIndexes();

    void saveAll(List<IndexSearchEntity> indexList);

    List<Integer> getLemmasIdsByPage(PageEntity page);

    void addPageIndexToDb(PageEntity page, Map<Integer, Integer> lemmaIdsAndFrequency);
}
