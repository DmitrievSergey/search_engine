package searchengine.dto.statistics;

import lombok.Value;

@Value
public class PageStatistic {
    String url;
    String content;
    int code;
}
