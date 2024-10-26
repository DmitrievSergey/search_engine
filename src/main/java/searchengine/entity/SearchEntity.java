package searchengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import searchengine.dto.statistics.SearchStatistic;

@Getter
@Setter
@Entity
@Table(name = "search")
public class SearchEntity implements Comparable<SearchEntity>{
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "query", nullable = false)
    private String query;
    @Column(name = "site", nullable = false)
    private String site;
    @Column(name = "site_name", nullable = false)
    private String siteName;
    @Column(name = "url", nullable = false)
    private String uri;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "snippet", columnDefinition = "TEXT", nullable = false)
    private String snippet;
    @Column(name = "relevance", nullable = false)
    private float relevance;

    public SearchEntity(String site, String siteName, String uri, String title, String snippet, float relevance) {
        this.site = site;
        this.siteName = siteName;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    public SearchEntity() {
    }

    public SearchStatistic entityToStatistic(){


        return new SearchStatistic(
                this.getSite(),
                this.getSiteName(),
                this.getUri(),
                this.getTitle(),
                this.getSnippet(),
                this.getRelevance()
        );
    }

    @Override
    public int compareTo(SearchEntity o) {
        Float th = this.getRelevance();
        Float oth = o.getRelevance();
        return th.compareTo(oth);
    }
}
