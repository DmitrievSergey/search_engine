package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.lucene.search.IndexSearcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.compare;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "page",
        indexes = {
                @Index(columnList = "path", name = "path_index")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"path", "site_id"})
        })
public class PageEntity implements Comparable<PageEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    SiteEntity site;

    String path;

    @Column(name = "code")
    int responseCode;

    String content;

    public PageEntity(SiteEntity site, String path, int responseCode, String content) {
        this.site = site;
        this.path = path;
        this.responseCode = responseCode;
        this.content = content;
    }

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<IndexSearchEntity> index = new ArrayList<>();



    @Override
    public int compareTo(PageEntity o) {
        Integer resPath = this.getPath().compareTo(o.getPath());
        Integer resSite = this.getSite().getId().compareTo(o.getSite().getId());
        return resPath.compareTo(resSite);
    }
}
