package searchengine.services.search;

import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
@Service
public interface SearchService {
    public static final String EMPTY_SEARCH_QUERY = "Задан пустой поисковый запрос";
    public static final String EMPTY_SEARCH_RESULT = "По запросу : \"%s\" найдено 0 результатов";

    SearchResponse search(String query, String site, Integer offset, Integer limit);

    boolean existsBySiteAndUri(String site, String uri);

    void deleteAll();

    void deleteAllBySiteAndUri(String site, String uri);
}
