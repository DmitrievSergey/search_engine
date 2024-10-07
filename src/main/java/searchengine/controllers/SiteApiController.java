package searchengine.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexindResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.site.SiteServiceImpl;


@AllArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api")
public class SiteApiController {
    private SitesList sitesList;
    private SiteServiceImpl siteService;
    private IndexingService indexingService;

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexindResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexingAllSites());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing() {

        return ResponseEntity.ok(indexingService.stopIndexingAllSites());
    }
}
