package searchengine.dto.statistics;

import lombok.Value;

@Value
public class LemmaStatistic {
    String lemma;
    int frequency;
}
