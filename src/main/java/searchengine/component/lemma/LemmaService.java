package searchengine.component.lemma;

import searchengine.dto.statistics.LemmaStatistic;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public interface LemmaService {
    List<LemmaStatistic> getLemmaDTOStatistic(SiteEntity site);
    NavigableMap<String, Integer> getPageLemmas(PageEntity page);
    List<LemmaEntity> saveAll(List<LemmaEntity> lemmaList);
    Map<String, LemmaEntity> getAllBySite(SiteEntity site);
    List<LemmaEntity> getLemmasFromQuery(String query, String site);
    void deleteAllLemmas();

    void deleteLemmasByIds(List<Integer> lemmasIds);

    Map<Integer, Integer> addPageLemmaToDb(PageEntity page, SiteEntity site);

    LemmaEntity getLemmaById(Integer key);
}
