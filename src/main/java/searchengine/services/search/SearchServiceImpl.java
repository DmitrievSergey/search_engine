package searchengine.services.search;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import searchengine.dto.mapper.SearchMapper;
import searchengine.dto.search.SearchResponse;
import searchengine.entity.*;
import searchengine.repositories.SearchRepository;
import searchengine.services.index.IndexService;
import searchengine.services.lemma.LemmaService;
import searchengine.services.morfology.MorphologyService;
import searchengine.services.site.SiteService;
import searchengine.services.snippet.SnippetService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Data
@Slf4j
@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService {
    private final SearchRepository searchRepository;
    private final MorphologyService morphologyService;
    private final LemmaService lemmaService;
    private final SiteService<SiteEntity> siteService;
    private final IndexService indexService;
    private final SnippetService snippetService;
    private final SearchMapper mapper;


    @Override
    public SearchResponse search(String query, String site, Integer offset, Integer limit) {
        if (query.isEmpty()) {
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setResult(false);
            searchResponse.setError(EMPTY_SEARCH_QUERY);
            log.info("In searchServiceImpl search: empty query");
            return searchResponse;
        }

        Pageable pageable = PageRequest.of(offset == 0 ? 0 : offset / limit, limit);
        log.info("In searchServiceImpl search: query - {}, offset - {}", query, offset);
        return getSearchResponse(query, site, pageable);
    }

    @Override
    public boolean existsBySiteAndUri(String site, String uri) {
        return searchRepository.existsBySiteAndUri(site, uri);
    }

    @Override
    public void deleteAll() {
        searchRepository.deleteAll();
    }

    @Override
    public void deleteAllBySiteAndUri(String site, String uri) {
        searchRepository.deleteAllBySiteAndUri(site, uri);
    }


    private SearchResponse getSearchResponse(String query, String site, Pageable pageable) {
        SearchResponse searchResponse = new SearchResponse();
        boolean exist = site != null ? existsByQueryAndSite(query, site) : existsByQuery(query);

        if (exist) {
            List<SearchEntity> data = getSearches(query, site, pageable);
            searchResponse.setData(data.stream().map(mapper::convertToDto).toList());
            searchResponse.setCount(getCount(query, site));

            return searchResponse;
        }

        List<SearchEntity> searchData = createSearchList(query, site);

        if (searchData.isEmpty()) {
            searchResponse.setResult(false);
            searchResponse.setError(String.format(EMPTY_SEARCH_RESULT, query));
            log.info("In searchServiceImpl search: not found any pages for query - {}", query);
            return searchResponse;
        }

        saveAll(searchData);

        searchResponse.setCount(getCount(query, site));
        searchResponse.setData(getSearches(query, site, pageable).stream().map(mapper::convertToDto).toList());

        return searchResponse;
    }

    private Integer getCount(String query, String site) {
        return site != null ? searchRepository.countByQueryAndSite(query, site) : searchRepository.countByQuery(query);
    }

    private List<SearchEntity> createSearchList(String query, String site) {
        List<LemmaEntity> lemmas = getLemmas(query, site);
        List<IndexEntity> indexes = getIndexes(lemmas);
        List<PageEntity> pages = indexes.stream().map(IndexEntity::getPage).toList();
        if (pages.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchEntity> searchData = getSearchData(lemmas, pages, indexes);
        searchData.forEach(s -> s.setQuery(query));

        return searchData;
    }

    private List<SearchEntity> getSearches(String query, String site, Pageable pageable) {
        return site != null ? searchRepository.findAllByQueryAndSite(query, site, pageable).getContent() :
                searchRepository.findAllByQuery(query, pageable).getContent();
    }

    private void saveAll(List<SearchEntity> searchData) {
        searchRepository.saveAll(searchData);
    }


    private List<LemmaEntity> getLemmas(String query, String site) {
        List<LemmaEntity> lemmas = new ArrayList<>();
        String[] words = query.split("\\s+");

        for (String world : words) {
            String normalForm = morphologyService.getNormalForm(world);

            if (normalForm.isEmpty()) {
                return new ArrayList<>();
            }

            lemmas.addAll(site != null ? lemmaService.getLemmasByLemmaAndSite(normalForm, siteService.findSiteByUrl(site)) :
                    lemmaService.getLemmasByLemma(normalForm));
        }

        return lemmas.stream()
                .filter(l -> l.getFrequency() < getAverageFrequency(lemmas) * 2)
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .toList();
    }

    private List<IndexEntity> getIndexes(List<LemmaEntity> lemmas) {
        if (lemmas.isEmpty()) {
            return new ArrayList<>();
        }

        List<IndexEntity> indexes = new ArrayList<>();

        for (LemmaEntity lemma : lemmas) {
            indexes.addAll(indexService.findAllByLemma(lemma));
        }

        return indexes;
    }

    private boolean existsByQuery(String query) {
        return searchRepository.existsByQuery(query);
    }

    private boolean existsByQueryAndSite(String query, String site) {
        return searchRepository.existsByQueryAndSite(query, site);
    }

    private int getAverageFrequency(List<LemmaEntity> lemmas) {
        int sumFrequency = 0;

        for (LemmaEntity lemma : lemmas) {
            sumFrequency += lemma.getFrequency();
        }

        return sumFrequency / lemmas.size();
    }

    private List<SearchEntity> getSearchData(List<LemmaEntity> lemmas, List<PageEntity> pages, List<IndexEntity> indexes) {
        List<SearchEntity> searchDataList = new ArrayList<>();
        float maxRelevance = getMaxRelevance(pages, indexes);

        pages.forEach(page -> {
            SiteEntity site = page.getSite();

            SearchEntity search = SearchEntity.builder()
                    .uri(page.getPath().equals("/") ? "" : page.getPath())
                    .site(site.getUrl())
                    .siteName(site.getName())
                    .title(getTitle(page.getContent()))
                    .snippet(snippetService.getSnippet(page, lemmas))
                    .relevance(getAbsolutRelevance(page, indexes) / maxRelevance)
                    .build();

            searchDataList.add(search);
        });

        searchDataList.sort(Comparator.comparing(SearchEntity::getRelevance).reversed());
        return searchDataList;
    }

    private String getTitle(String content) {
        return Jsoup.parse(content).title();
    }

    private float getAbsolutRelevance(PageEntity page, List<IndexEntity> indexes) {
        return indexes.stream()
                .filter(index -> index.getPage().equals(page))
                .mapToInt(IndexEntity::getRank)
                .sum();
    }


    private float getMaxRelevance(List<PageEntity> pages, List<IndexEntity> indexes) {
        float maxRelevance = 0;

        for (PageEntity page : pages) {
            List<IndexEntity> localIndexes = indexes.stream()
                    .filter(i -> i.getPage().equals(page))
                    .toList();

            float absolutRelevance = getAbsolutRelevance(page, localIndexes);
            maxRelevance = Math.max(absolutRelevance, maxRelevance);
        }

        return maxRelevance;
    }
}
