package searchengine.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexindResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistic.StatisticsService;
@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexindResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexingAllSites());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexindResponse> stopIndexing() {

        return ResponseEntity.ok(indexingService.stopIndexingAllSites());
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("In ApiController statistics");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexindResponse> addOrUpdatePage(@RequestParam(value = "url") String url) {
        log.info("In ApiController addOrUpdatePage: data with url - {} added or updated", url);
        return ResponseEntity.ok(indexingService.startIndexingPage(url.trim()));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam(value = "query") String query,
                                                 @RequestParam(value = "site", required = false) String site,
                                                 @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                 @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        log.info("In ApiController search: query - {}", query);
        return ResponseEntity.ok(searchService.search(query.trim(), site, offset, limit));
    }
}
