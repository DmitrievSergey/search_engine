package searchengine.services.index;

import org.springframework.stereotype.Service;
import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.repositories.IndexRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Service
public class IndexServiceImpl implements IndexService{
    private final IndexRepository repository;

    public IndexServiceImpl(IndexRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createIndexes(Map<LemmaEntity, Integer> lemmaIntegerMap, PageEntity page) {
        Set<LemmaEntity> lemmas = lemmaIntegerMap.keySet();
        List<IndexEntity> indexes = new ArrayList<>();
        for (LemmaEntity l : lemmas) {
            IndexEntity index = IndexEntity
                    .builder()
                    .page(page)
                    .rank(lemmaIntegerMap.get(l))
                    .lemma(l)
                    .build();
            indexes.add(index);
        }
        repository.saveAll(indexes);
    }

    @Override
    public void deleteAllIndices() {
        repository.deleteAllIndices();
    }

    @Override
    public void deleteAllByEntities(List<IndexEntity> indices) {
        repository.deleteAll();
    }

    @Override
    public List<IndexEntity> findAllByPage(PageEntity page) {
        return repository.findAllByPage(page);
    }

    @Override
    public List<IndexEntity> findAllByLemma(LemmaEntity lemma) {
        return repository.findAllByLemma(lemma);
    }
}
