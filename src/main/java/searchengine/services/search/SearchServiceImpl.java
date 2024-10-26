package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.entity.*;
import searchengine.repositories.SearchRepository;
import searchengine.component.site.SiteService;
import searchengine.component.index.IndexService;
import searchengine.component.lemma.LemmaService;
import searchengine.component.morfology.MorphologyService;
import searchengine.component.page.PageService;
import searchengine.component.snippet.SnippetService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    private static Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    private final SearchRepository searchRepository;
    private final MorphologyService morphologyService;
    private final LemmaService lemmaService;
    private final SiteService<SiteEntity> siteService;
    private final IndexService indexService;
    private final PageService<PageEntity> pageService;
    private final SnippetService snippetService;


    @Override
    public SearchResponse search(String query, String site, Integer offset, Integer limit) {
        if (query.isEmpty()) {
            logger.info("In searchServiceImpl search: empty query");
            return new SearchResponse(false, SearchResponse.EMPTY_REQUEST);
        }

        Pageable pageable = PageRequest.of(offset == 0 ? 0 : offset / limit, limit);
        logger.info("In searchServiceImpl search: query - {}, offset - {}", query, offset);
        return getSearchResponse(query, site, pageable);
    }

    private SearchResponse getSearchResponse(String query, String site, Pageable pageable) {
        SearchResponse searchResponse = new SearchResponse();
        boolean exist = site != null ? existsByQueryAndSite(query, site) : existsByQuery(query);

        if (exist) {
            List<SearchEntity> data = getSearches(query, site, pageable);
            searchResponse.setData(data.parallelStream().map(SearchEntity::entityToStatistic).toList());
            searchResponse.setCount(getCount(query, site));

            return searchResponse;
        }

        List<SearchEntity> searchData = createSearchList(query, site);

        if (searchData.isEmpty()) {
            searchResponse.setResult(false);
            searchResponse.setError(String.format(SearchResponse.EMPTY_RESULT, query));
            logger.info("In searchServiceImpl search: not found any pages for query - {}", query);
            return searchResponse;
        }
        if(!searchRepository.existsByQuery(query)) saveAll(searchData);

        searchResponse.setCount(getCount(query, site));
        searchResponse.setData(getSearches(query, site, pageable)
                .parallelStream().map(SearchEntity::entityToStatistic).toList());

        return searchResponse;
    }

    private Integer getCount(String query, String site) {
        logger.info("Size of site" + site.length());
        return !site.isEmpty() ? searchRepository.findAllByQueryAndSite(query, site).size() :
                searchRepository.findAllByQuery(query).size();
    }

    private List<SearchEntity> createSearchList(String query, String site) {
        List<LemmaEntity> lemmas = lemmaService.getLemmasFromQuery(query, site);
        logger.debug("Получили леммы {}",lemmas.size());
        List<IndexSearchEntity> indexes = indexService.getIndexSearchesByLemmas(lemmas);
        logger.debug("Получили индексы {}",indexes.size());
        List<PageEntity> pages = pageService.getPagesByIds(indexes.stream().map(indexSearchEntity -> {
            return indexSearchEntity.getPage().getId();
        }).toList());
        logger.info(pages.toString());
        logger.debug("Получили страницы {}",pages.size());
        if (pages.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchEntity> searchData = getSearchData(lemmas, pages, indexes);
        logger.info("Размер searchData - " + searchData);
        searchData.forEach(s -> s.setQuery(query));

        return searchData;
    }

    private List<SearchEntity> getSearches(String query, String site, Pageable pageable) {
        return !site.isEmpty() ? searchRepository.findAllByQueryAndSite(query, site, pageable).getContent() :
                searchRepository.findAllByQuery(query, pageable).getContent();
    }

    private void saveAll(List<SearchEntity> searchData) {
        searchRepository.flush();
        searchRepository.saveAll(searchData);
    }



    private boolean existsByQuery(String query) {
        return searchRepository.existsByQuery(query);
    }

    private boolean existsByQueryAndSite(String query, String site) {
        return searchRepository.existsByQueryAndSite(query, site);
    }

    private List<SearchEntity> getSearchData(List<LemmaEntity> lemmas, List<PageEntity> pages, List<IndexSearchEntity> indexes) {
        logger.info("Зашли в getSearchData ");
        List<SearchEntity> searchDataList = new ArrayList<>();
        float maxRelevance = getMaxRelevance(pages, indexes);
        pages.parallelStream().forEach(page -> {
            SiteEntity site = siteService.findSiteBySiteId(page.getSite().getId());
            SearchEntity search = new SearchEntity(
                    site.getUrl(),
                    site.getName(),
                    page.getPath().equals("/") ? "" : page.getPath(),
                    getTitle(page.getContent()),
                    snippetService.getSnippet(page, lemmas),
                    getAbsolutRelevance(page, indexes) / maxRelevance);


            searchDataList.add(search);
        });



        return searchDataList.stream().sorted().toList();
    }

    private String getTitle(String content) {
        return Jsoup.parse(content).title();
    }

    private float getAbsolutRelevance(PageEntity page, List<IndexSearchEntity> indexes) {
        return indexes.stream()
                .filter(index -> index.getPage().equals(page))
                .mapToInt(IndexSearchEntity::getRank)
                .sum();
    }


    private float getMaxRelevance(List<PageEntity> pages, List<IndexSearchEntity> indexes) {
        float maxRelevance = 0;

        for (PageEntity page : pages) {
            List<IndexSearchEntity> localIndexes = indexes.stream()
                    .filter(i -> i.getPage().getId().equals(page.getId()))
                    .toList();

            float absolutRelevance = getAbsolutRelevance(page, localIndexes);
            maxRelevance = Math.max(absolutRelevance, maxRelevance);
        }

        return maxRelevance;
    }
}
