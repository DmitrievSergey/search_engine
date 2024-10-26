package searchengine.dto.statistics;

import lombok.Value;

@Value
public class IndexStatistic {
    Integer pageId;
    Integer lemmaId;
    Float rank;
}
