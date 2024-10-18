package searchengine.services.lemma;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.entity.LemmaEntity;
import searchengine.entity.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.services.indexing.IndexingService;
import searchengine.services.morfology.MorphologyService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {

    private final LemmaRepository lemmaRepository;
    private final MorphologyService morphologyService;


    @Override
    public void add(LemmaEntity lemma) {
        lemmaRepository.save(lemma);
    }

    @Override
    public Map<LemmaEntity, Integer> getLemmas(Document doc, SiteEntity site) {
        Map<LemmaEntity, Integer> lemmaIntegerMap = new HashMap<>();
        String[] words = doc.text().split("\\s+");
        for (String word : words) {
            String normalForm = morphologyService.getNormalForm(word);

            if (normalForm.isEmpty()) {
                continue;
            }

            LemmaEntity lemma = LemmaEntity
                    .builder()
                    .site(site)
                    .lemma(normalForm)
                    .frequency(1)
                    .build();

            if (lemmaIntegerMap.containsKey(lemma)) {
                lemmaIntegerMap.put(lemma, lemmaIntegerMap.get(lemma) + 1);
            } else {
                lemmaIntegerMap.put(lemma, 1);
            }
        }
        return addAll(lemmaIntegerMap, site);
    }

    @Override
    public Boolean existsByLemmaAndSite(String lemma, SiteEntity site) {
        return lemmaRepository.existsByLemmaAndSite(lemma, site);
    }

    @Override
    public Long getCount() {
        return lemmaRepository.count();
    }

    @Override
    public Long getCountBySite(SiteEntity site) {
        return lemmaRepository.countBySite(site);
    }

    @Override
    public List<LemmaEntity> getLemmasByLemmaAndSite(String lemma, SiteEntity site) {
        return lemmaRepository.findAllByLemmaAndSite(lemma, site);
    }

    @Override
    public List<LemmaEntity> getLemmasByLemma(String normalForms) {
        return lemmaRepository.findAllByLemma(normalForms);
    }

    @Override
    public void delete(LemmaEntity lemma) {
        lemmaRepository.delete(lemma);
    }

    @Override
    public void deleteAllLemmas() {
        lemmaRepository.deleteAllLemmas();
    }


    private Map<LemmaEntity, Integer> addAll(Map<LemmaEntity, Integer> lemmasMap, SiteEntity site) {
        Map<LemmaEntity, Integer> result = new HashMap<>();
        Set<LemmaEntity> lemmas = lemmasMap.keySet();
        for (LemmaEntity l : lemmas) {
            if (IndexingService.isIndexingStopped.get()) {
                break;
            }

            if (!lemmaRepository.existsByLemmaAndSite(l.getLemma(), site)) {

                    lemmaRepository.save(l);
                    result.put(l, lemmasMap.get(l));
                    continue;

            }
            LemmaEntity lemma = lemmaRepository.findByLemmaAndSite(l.getLemma(), site);
            lemma.setFrequency(lemma.getFrequency() + 1);
            lemmaRepository.save(lemma);
            result.put(lemma, lemmasMap.get(l));
        }

        return result;
    }
}
