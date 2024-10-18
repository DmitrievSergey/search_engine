package searchengine.services.lemma;

import org.jsoup.nodes.Document;
import searchengine.entity.LemmaEntity;
import searchengine.entity.SiteEntity;

import java.util.List;
import java.util.Map;

public interface LemmaService {
    void add(LemmaEntity lemma);

    Map<LemmaEntity, Integer> getLemmas(Document doc, SiteEntity site);

    Boolean existsByLemmaAndSite(String lemma, SiteEntity site);

    Long getCount();

    Long getCountBySite(SiteEntity site);

    List<LemmaEntity> getLemmasByLemmaAndSite(String lemma, SiteEntity site);

    List<LemmaEntity> getLemmasByLemma(String normalForms);

    void delete(LemmaEntity lemma);

    void deleteAllLemmas();
}
