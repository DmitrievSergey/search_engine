package searchengine.component.lemma;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import searchengine.component.morfology.MorphologyService;
import searchengine.dto.statistics.LemmaStatistic;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.Status;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.component.site.SiteService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {
    private static Logger logger = LoggerFactory.getLogger(LemmaServiceImpl.class);


    private final LemmaRepository lemmaRepository;
    private final MorphologyService morphologyService;
    private final SiteService<SiteEntity> siteService;
    private final PageRepository pageRepository;


    @Override
    public List<LemmaStatistic> getLemmaDTOStatistic(SiteEntity site) {
        List<LemmaStatistic> lemmaStatisticList = new CopyOnWriteArrayList<>();
        ConcurrentSkipListMap<String, Integer> totalSiteLemmas = new ConcurrentSkipListMap<>();
        logger.info("Начали сбор лемм по сайту {} ", site.getName());

        List<PageEntity> pageList = pageRepository.findAllBySiteIdAndResponseCode(site.getId());
        logger.info("Завершили доставать страницы по сайту {} ", site.getName());
        for (PageEntity page : pageList) {
            for (Map.Entry<String, Integer> entry : getPageLemmas(page).entrySet()) {

                Integer retFreq = totalSiteLemmas.put(entry.getKey(), 1);
                if (retFreq == null) continue;
                totalSiteLemmas.put(entry.getKey(), retFreq + 1);

            }
        }

        logger.info("Количество лемм " + totalSiteLemmas.size());
        logger.info("Начали итерироваться по мапе");
        for (Map.Entry<String, Integer> entry : totalSiteLemmas.entrySet()) {
            lemmaStatisticList.add(new LemmaStatistic(entry.getKey(), entry.getValue()));
        }
        logger.info("Завершили сбор {} ", lemmaStatisticList.size());
        return lemmaStatisticList;
    }


    @Override
    public NavigableMap<String, Integer> getPageLemmas(PageEntity page) {
        ConcurrentNavigableMap<String, Integer> lemmaFrequency = new ConcurrentSkipListMap<>();
        Document document = Jsoup.parse(page.getContent());
        String[] words = document.text().split("\\s+");
        document = null;
        if (words.length == 0) return Collections.emptyNavigableMap();
        Arrays.stream(words).parallel().forEach(s -> {
            String normalForm = morphologyService.getNormalForm(s);
            if (!normalForm.isEmpty()) {
                if (lemmaFrequency.put(normalForm, 1) != null) {
                    lemmaFrequency.put(normalForm, lemmaFrequency.get(normalForm) + 1);
                }
            }
        });

        return lemmaFrequency;
    }
    @Override
    public Map<SiteEntity, List<LemmaEntity>> getLemmasFromQuery(String query) {
        CopyOnWriteArrayList<LemmaEntity> queryLemmas = new CopyOnWriteArrayList<>();
        List<String> normalForms = getNormalFormsForQuery(query);
        List<SiteEntity> listIndexedSites = siteService.findSitesWithStatusIndexed();
        //в таблице лемма ищем леммы по каждому сайту
        Map<SiteEntity, List<LemmaEntity>> siteLemmasMap = new ConcurrentHashMap<>();
        listIndexedSites.parallelStream().forEach(s -> {
            CopyOnWriteArrayList<LemmaEntity> siteLemmas = new CopyOnWriteArrayList<>();
            normalForms.parallelStream().forEach(n -> {
                LemmaEntity siteLemma = lemmaRepository.findLemmaEntityByLemmaAndSiteId(n, s.getId());
                if (siteLemma != null) siteLemmas.add(siteLemma);
            });
            siteLemmasMap.put(s,getSortedAndFilteredLemmas(siteLemmas));
            siteLemmas.clear();
            normalForms.clear();
        });
        // на выходе получаем наверное сортид мапу сайт - отсортированный список лемм и вес по сайтам

    return siteLemmasMap;
    }

    private List<LemmaEntity> getSortedAndFilteredLemmas(List<LemmaEntity> lemmaList) {
        float aLemma = getAverageFrequency(lemmaList) * 2;
        return lemmaList.stream()
                .sorted()
                .filter(l -> l.getFrequency() < aLemma)
                .toList();
    }

    private List<String> getNormalFormsForQuery(String query) {
        CopyOnWriteArrayList<String> normalForms = new CopyOnWriteArrayList<>();
        String[] words = query.split("\\s+");
        Arrays.stream(words).parallel().forEach(s -> {
            String normalForm = morphologyService.getNormalForm(s);
            normalForms.add(normalForm);
        });
        return normalForms;
    }

    @Override
    public List<LemmaEntity> getLemmasFromQuery(String query, String site) {
        CopyOnWriteArrayList<LemmaEntity> queryLemmas = new CopyOnWriteArrayList<>();
        String[] words = query.split("\\s+");

        SiteEntity siteEntity = siteService.findSiteByUrl(site);
        Arrays.stream(words).parallel().forEach(s -> {
            String normalForm = morphologyService.getNormalForm(s);
            if (!normalForm.isEmpty()) {
                queryLemmas.add(
                        lemmaRepository.findLemmaEntityByLemmaAndSiteId(normalForm, siteEntity.getId())
                );
            }
        });
        try {
            return queryLemmas.stream()
                    .filter(l -> l.getFrequency() < getAverageFrequency(queryLemmas) * 2)
                    .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                    .toList();
        } catch (NullPointerException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteAllLemmas() {
        lemmaRepository.deleteAllLemmas();
    }

    @Override
    public void deleteLemmasByIds(List<Integer> lemmasIds) {
        lemmasIds.parallelStream().forEach(i -> {
            LemmaEntity lemma = lemmaRepository.findById(i);
            if (lemma.getFrequency() > 1) {
                lemma.setFrequency(lemma.getFrequency() - 1);
                lemmaRepository.save(lemma);
            } else {
                lemmaRepository.delete(lemma);
            }
        });

    }

    @Override
    public Map<Integer, Integer> addPageLemmaToDb(PageEntity page, SiteEntity site) {
        Map<String, Integer> map = getPageLemmas(page);
        Map<Integer, Integer> lemmasIdsAndFrequency = new HashMap<>();
        List<LemmaEntity> lemmaList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            LemmaEntity lemma = lemmaRepository.findLemmaEntityByLemmaAndSiteId(
                    entry.getKey()
                    , site.getId());
            if (lemma == null) {
                LemmaEntity newLemma = new LemmaEntity(
                        site,
                        entry.getKey(),
                        entry.getValue()
                );
                //lemmaList.add(newLemma);
                newLemma = lemmaRepository.save(newLemma);
                lemmasIdsAndFrequency.put(newLemma.getId(), entry.getValue());
            } else {
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);
                //lemmaList.add(lemma);
                lemmasIdsAndFrequency.put(lemma.getId(), entry.getValue());
            }
        }
        //saveAll(lemmaList);
        //lemmaList.clear();
        return lemmasIdsAndFrequency;
    }

    @Override
    public LemmaEntity getLemmaById(Integer key) {
        return lemmaRepository.findById(key);
    }

    @Override
    public List<LemmaEntity> saveAll(List<LemmaEntity> lemmaList) {
        lemmaRepository.flush();
        return lemmaRepository.saveAll(lemmaList);
    }

    @Override
    public Map<String, LemmaEntity> getAllBySite(SiteEntity site) {
        return lemmaRepository.findLemmaEntityBySite(site.getId())
                .stream()
                .collect(Collectors.toMap(LemmaEntity::getLemma, Function.identity()));
    }


    private int getAverageFrequency(List<LemmaEntity> lemmas) throws NullPointerException {
        if (lemmas.isEmpty()) throw new NullPointerException();
        int sumFrequency = 0;

        for (LemmaEntity lemma : lemmas) {
            sumFrequency += lemma.getFrequency();
        }

        return sumFrequency / lemmas.size();
    }

}
