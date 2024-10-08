package searchengine.services.indexing;

import org.springframework.http.ResponseEntity;
import searchengine.dto.indexing.IndexindResponse;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IndexingService<Che> {
    public static AtomicBoolean isIndexingStopped = new AtomicBoolean(false);
    IndexindResponse startIndexingAllSites();
    IndexindResponse stopIndexingAllSites();
    IndexindResponse startIndexingPage(String url);
}
