package searchengine.dto.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchStatistic implements Comparable<SearchStatistic> {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private Float relevance;

    public SearchStatistic(String address, String siteName, String uri,
                            String title, String snippet, Float relevance) {
        this.site = address;
        this.siteName = siteName;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    @Override
    public int compareTo(SearchStatistic o) {
        return o.getRelevance().compareTo(this.getRelevance());
    }
}
