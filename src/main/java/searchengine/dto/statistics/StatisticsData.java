package searchengine.dto.statistics;

import lombok.Data;
import lombok.Value;

import java.util.List;

@Value
public class StatisticsData {
    TotalStatistics total;
    List<DetailedStatisticsItem> detailed;

}
