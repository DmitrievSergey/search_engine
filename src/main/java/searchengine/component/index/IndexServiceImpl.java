package searchengine.component.index;

import org.springframework.stereotype.Component;
import searchengine.component.index.IndexService;
import searchengine.entity.IndexSearchEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.component.lemma.LemmaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class IndexServiceImpl implements IndexService {
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    public IndexServiceImpl(LemmaRepository lemmaRepository, PageRepository pageRepository, LemmaService lemmaService, IndexRepository indexRepository) {
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
        this.lemmaService = lemmaService;
        this.indexRepository = indexRepository;
    }
    @Override
    public List<IndexSearchEntity> getIndexSearchesByLemmas(List<LemmaEntity> lemmas) {
        if (lemmas.isEmpty()) {
            return new ArrayList<>();
        }

        List<IndexSearchEntity> indexes = new CopyOnWriteArrayList<>();
        lemmas.parallelStream().forEach(lemmaEntity -> {
            indexes.addAll(indexRepository.findAllByLemma(lemmaEntity));
        });

        return indexes;
    }


    @Override
    public void deleteByPageEntity(PageEntity page) {
        List<IndexSearchEntity> pageIndex = indexRepository.findAllByPage(page);
        indexRepository.deleteAllInBatch(pageIndex);
    }

    @Override
    public void deleteAllIndexes() {
        indexRepository.deleteAllIndexes();
    }

    @Override
    public void saveAll(List<IndexSearchEntity> indexList) {
        try {
            indexRepository.flush();
            indexRepository.saveAll(indexList);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Integer> getLemmasIdsByPage(PageEntity page) {
        return indexRepository.findLemmasIdByPage(page);
    }

    @Override
    public void addPageIndexToDb(PageEntity page, Map<Integer, Integer> lemmaIdsAndFrequency) {
        List<IndexSearchEntity> indexList = new ArrayList<>();
        for(Map.Entry<Integer, Integer> entry : lemmaIdsAndFrequency.entrySet()) {
            LemmaEntity entity = lemmaService.getLemmaById(entry.getKey());
            IndexSearchEntity index = new IndexSearchEntity(
                    page,
                    entity,
                    entry.getValue()
                    );
            indexList.add(index);
        }
        indexRepository.saveAll(indexList);

    }
}
