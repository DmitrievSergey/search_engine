package searchengine.dto.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchStatistic {
    private String address;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private Float relevance;

    public SearchStatistic(String address, String siteName, String uri,
                            String title, String snippet, Float relevance) {
        this.address = address;
        this.siteName = siteName;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }
}
