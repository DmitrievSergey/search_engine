package searchengine.services.indexing;

import org.springframework.http.ResponseEntity;
import searchengine.dto.indexing.IndexindResponse;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IndexingService {

    int CORES = Runtime.getRuntime().availableProcessors();

    AtomicBoolean isIndexingStopped = new AtomicBoolean(false);
    AtomicBoolean isIndexingRunning = new AtomicBoolean(false);
    IndexindResponse startIndexingAllSites();
    IndexindResponse stopIndexingAllSites();
    IndexindResponse startIndexingPage(String url);
}
