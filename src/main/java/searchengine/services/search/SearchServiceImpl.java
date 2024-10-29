package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import searchengine.dto.IndexDto;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.SearchStatistic;
import searchengine.entity.*;
import searchengine.repositories.SearchRepository;
import searchengine.component.site.SiteService;
import searchengine.component.index.IndexService;
import searchengine.component.lemma.LemmaService;
import searchengine.component.morfology.MorphologyService;
import searchengine.component.page.PageService;
import searchengine.component.snippet.SnippetService;
import searchengine.services.indexing.IndexingService;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
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
        if (query.isBlank()) {
            logger.info("In searchServiceImpl search: empty query");
            return new SearchResponse(false, SearchResponse.EMPTY_REQUEST);
        }

        if (IndexingService.isIndexingRunning.get())
            return new SearchResponse(false, SearchResponse.EMPTY_RESULT);

        if (!site.isEmpty() && (siteService.findSiteByUrl(site) == null ||
                siteService.findSiteByUrl(site).getStatus() != Status.INDEXED))
            return new SearchResponse(false, SearchResponse.EMPTY_RESULT);

        Pageable pageable = PageRequest.of(offset == 0 ? 0 : offset / limit, limit);
        logger.info("In searchServiceImpl search: query - {}, offset - {}", query, offset);
        return getSearchResponse(query, site, pageable);
    }

    @Override
    public void deleteSearchData() {
        searchRepository.clearSearch();
    }

    private SearchResponse getSearchResponse(String query, String site, Pageable pageable) {
        boolean exist = !site.isEmpty();
        if(exist) {
            if(!existsByQueryAndSite(query, site)) createQueryResult(query);
            return getResponseResult(query, site, pageable);
        }
        if(!existsByQuery(query)) createQueryResult(query);





        return getResponseResult(query, pageable);
    }

    private SearchResponse getResponseResult(String query, String site, Pageable pageable){
        SearchResponse searchResponse = new SearchResponse();
        List<SearchEntity> fullList = getQueryFullQueryResult(query, site);
        List<SearchStatistic> resultList = new CopyOnWriteArrayList<>();
        Integer maxRel = fullList.stream().map(SearchEntity::getRelevance).mapToInt(Float::intValue).sum();
        List<SearchEntity> list = getQueryResults(query, site, pageable);
        list.parallelStream().forEach(e -> {
            e.setQuery(query);
            e.setRelevance((e.getRelevance()/maxRel));
            resultList.add(e.entityToStatistic());
        });
        searchResponse.setCount(fullList.size());
        searchResponse.setData(resultList.stream().sorted().toList());
        searchResponse.setResult(true);
        return searchResponse;
    }

    private SearchResponse getResponseResult(String query, Pageable pageable) {
        SearchResponse searchResponse = new SearchResponse();
        List<SearchStatistic> resultList = new CopyOnWriteArrayList<>();
        List<SearchEntity> fullList = getQueryFullQueryResult(query);
        Integer maxRel = fullList.stream().map(SearchEntity::getRelevance).mapToInt(Float::intValue).sum();
        List<SearchEntity> list = getQueryResults(query, pageable);
        list.parallelStream().forEach(e -> {
            e.setQuery(query);
            e.setRelevance((e.getRelevance()/maxRel));
            resultList.add(e.entityToStatistic());
        });
        searchResponse.setCount(fullList.size());
        searchResponse.setData(resultList.stream().sorted().toList());
        searchResponse.setResult(true);

        return searchResponse;
    }

    private List<SearchEntity> getQueryFullQueryResult(String query, String site) {
        return searchRepository.findAllByQueryAndSite(query, site);
    }

    List<SearchEntity> getQueryFullQueryResult(String query) {
        return searchRepository.findAllByQuery(query);
    }

    private void createQueryResult(String query) {
        Map<SiteEntity, List<LemmaEntity>> lemmaEntityList = lemmaService.getLemmasFromQuery(query);
        if(lemmaEntityList.isEmpty()) return;
        List<SearchEntity> searchEntityList = new CopyOnWriteArrayList<>();
        lemmaEntityList.entrySet().parallelStream().forEach(e -> {
            List<IndexSearchEntity> indexList = getSearchIndexes(e.getValue());
            if (!indexList.isEmpty()) {
                try {

                    Map<PageEntity, Integer> map = getMapPageMaxRelevance(indexList);

                    map.entrySet().parallelStream().forEach(p -> {
                        SiteEntity site = e.getKey();
                        logger.info("p.getKey().getId() {}",p.getKey().getId());
                        PageEntity page = pageService.getPageById(p.getKey().getId());
                        logger.info("page {}",page);
                        SearchEntity search = new SearchEntity(
                                query,
                                site.getUrl(),
                                site.getName(),
                                page.getPath(),
                                getTitle(page.getContent()),
                                snippetService.getSnippet(page, e.getValue()),
                                p.getValue().floatValue()
                        );
                        logger.info(" Search {}", search.toString());
                        searchEntityList.add(search);
                    });


                logger.info("Save query result for {} search map size {} ", e.getKey().getName(), searchEntityList.size());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        if (!searchRepository.existsByQuery(query)) saveAll(searchEntityList);

    }

    private Map<PageEntity, Integer> getMapPageMaxRelevance(List<IndexSearchEntity> indexList) {
        Map<PageEntity, Integer> map = new HashMap<>();

            for(IndexSearchEntity index: indexList) {
                Integer prevValue = map.put(index.getPage(), index.getRank());
                if(prevValue == null) continue;
                map.put(index.getPage(), index.getRank() + prevValue);
            }
        logger.info("getMapPageMaxRelevance size {}", map.size());
            return map;
    }

    private List<IndexSearchEntity> getSearchIndexes(List<LemmaEntity> listSearchLemmas) {
        List<IndexSearchEntity> finalList = new ArrayList<>();
        List<IndexDto> indexDtoList = new ArrayList<>();
        if(listSearchLemmas.size() == 1) return indexService.getIndexSearchesByLemma(listSearchLemmas.get(0));
        for (int i = 0; i < listSearchLemmas.size(); i = i + 2) {
            List<IndexDto> list1 = indexService.getIndexSearchesByLemma(listSearchLemmas.get(i))
                    .parallelStream().map(this::convertEntityToDto).collect(Collectors.toList());

            if (i == 0) {
                List<IndexDto> list2 = indexService.getIndexSearchesByLemma(listSearchLemmas.get(i + 1))
                        .parallelStream().map(this::convertEntityToDto).collect(Collectors.toList());
                logger.info(list1.get(0) + " " + list2.get(0));
                list1.retainAll(list2);
                indexDtoList.addAll(list1);
                list2.clear();
            } else {
                indexDtoList.retainAll(list1);
            }
            list1.clear();
        }
        finalList = indexDtoList.parallelStream().map(this::convertDtoToEntity).toList();
        indexDtoList.clear();
        return finalList;
    }

//    private SearchResponse getSearchResponse(String query, String site, Pageable pageable) {
//        SearchResponse searchResponse = new SearchResponse();
//        boolean exist = !site.isEmpty() ? existsByQueryAndSite(query, site) : existsByQuery(query);
//
//        if (exist) {
//            List<SearchEntity> data = getQueryResults(query, site, pageable);
//            searchResponse.setData(data.parallelStream().map(SearchEntity::entityToStatistic).toList());
//            searchResponse.setCount(getCount(query, site));
//
//            return searchResponse;
//        }
//
//        List<SearchEntity> searchData = createSearchList(query, site);
//
//        if (searchData.isEmpty()) {
//            searchResponse.setResult(false);
//            searchResponse.setError(String.format(SearchResponse.EMPTY_RESULT, query));
//            logger.info("In searchServiceImpl search: not found any pages for query - {}", query);
//            return searchResponse;
//        }
//        if (!searchRepository.existsByQuery(query)) saveAll(searchData);
//
//        searchResponse.setCount(getCount(query, site));
//        searchResponse.setData(getQueryResults(query, site, pageable)
//                .parallelStream().map(SearchEntity::entityToStatistic).toList());
//
//        return searchResponse;
//    }

    private Integer getCount(String query, String site) {
        logger.info("Size of site" + site.length());
        return !site.isEmpty() ? searchRepository.findAllByQueryAndSite(query, site).size() :
                searchRepository.findAllByQuery(query).size();
    }

    private List<SearchEntity> createSearchList(String query, String site) {
        List<LemmaEntity> lemmas = lemmaService.getLemmasFromQuery(query, site);
        logger.debug("Получили леммы {}", lemmas.size());
        List<IndexSearchEntity> indexes = indexService.getIndexSearchesByLemmas(lemmas);
        logger.debug("Получили индексы {}", indexes.size());
        List<PageEntity> pages = pageService.getPagesByIds(indexes.stream().map(indexSearchEntity -> {
            return indexSearchEntity.getPage().getId();
        }).toList());
        logger.debug(pages.toString());
        logger.debug("Получили страницы {}", pages.size());
        if (pages.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchEntity> searchData = getSearchData(lemmas, pages, indexes);
        logger.debug("Размер searchData - " + searchData);
        searchData.forEach(s -> s.setQuery(query));

        return searchData;
    }

    private List<SearchEntity> getQueryResults(String query, String site, Pageable pageable) {
        return searchRepository.findAllByQueryAndSite(query, site, pageable).getContent();
    }

    private List<SearchEntity> getQueryResults(String query, Pageable pageable) {
        return searchRepository.findAllByQuery(query, pageable).getContent();
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
        String query = " ";
        pages.parallelStream().forEach(page -> {
            SiteEntity site = siteService.findSiteBySiteId(page.getSite().getId());
//            SearchEntity search = new SearchEntity(
//                    site.getUrl(),
//                    site.getName(),
//                    page.getPath().equals("/") ? "" : page.getPath(),
//                    getTitle(page.getContent()),
//                    snippetService.getSnippet(page, lemmas)
//            );



            //searchDataList.add(search);
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

    private IndexDto convertEntityToDto(IndexSearchEntity index){
        IndexDto indexDto = new IndexDto();
        indexDto.setPageId(index.getPage().getId());
        indexDto.setLemmaId(index.getLemma().getId());
        indexDto.setRank(index.getRank());
        return indexDto;
    }

    private IndexSearchEntity convertDtoToEntity(IndexDto indexDto) {
        IndexSearchEntity index = new IndexSearchEntity();
        index.setPage(pageService.getPageById(indexDto.getPageId()));
        index.setLemma(lemmaService.getLemmaById(indexDto.getLemmaId()));
        index.setRank(indexDto.getRank());
        index.setId(indexService.
                getIndexByPageIdAndLemmaId(indexDto.getPageId(), indexDto.getLemmaId())
                .getId());
        return index;
    }


}
