package searchengine.dto.statistics;

import lombok.Data;
import lombok.Value;

@Value
public class StatisticsResponse {
    boolean result;
    StatisticsData statistics;
}
