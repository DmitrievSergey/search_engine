package searchengine.dto.statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TotalStatistics {
    private Long sites;
    private Long pages;
    private Long lemmas;
    private boolean indexing;
}
