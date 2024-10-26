package searchengine.dto.statistics;

import lombok.Value;
import searchengine.entity.Status;

import java.time.LocalDateTime;

@Value
public class DetailedStatisticsItem {
    String url;
    String name;
    Status status;
    LocalDateTime statusTime;
    String error;
    Long pages;
    Long lemmas;
}
