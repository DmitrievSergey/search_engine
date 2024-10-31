package searchengine.component.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import searchengine.component.index.IndexService;
import searchengine.component.lemma.LemmaServiceImpl;
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
    private static Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

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
        } catch(DataIntegrityViolationException e) {
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
            logger.info("Entry lemmaId {} frequency {}", entry.getKey(), entry.getValue());
            LemmaEntity entity = lemmaService.getLemmaById(entry.getKey());
            logger.info("LemmaEntity = {}", entity.getLemma());
            IndexSearchEntity index = new IndexSearchEntity(
                    page,
                    entity,
                    entry.getValue()
                    );
            //indexList.add(index);
            try {
                indexRepository.save(index);
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                logger.info("Получили ошибку на индексе {}", index.getLemma());
            }
        }

        //saveAll(indexList);

    }


    @Override
    public List<IndexSearchEntity> getIndexSearchesByLemma(LemmaEntity lemma) {
        return indexRepository.findAllByLemma(lemma);
    }

    @Override
    public IndexSearchEntity getIndexByPageIdAndLemmaId(int pageId, int lemmaId) {
        return indexRepository.getIndexByPageIdAndLemmaId(pageId, lemmaId);
    }

}
