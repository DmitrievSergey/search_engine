package searchengine.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexindResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.BadRequest;
import searchengine.dto.statistics.SearchStatistic;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistic.StatisticsService;

import java.util.List;

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
    public ResponseEntity<SearchResponse> search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                                 @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                                 @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                 @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        log.info("In ApiController search: query - {}", query);
        return ResponseEntity.ok(searchService.search(query.trim(), site, offset, limit));
    }
}
