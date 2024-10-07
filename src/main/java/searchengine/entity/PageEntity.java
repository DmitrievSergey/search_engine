package searchengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

import static java.lang.Integer.compare;

@Getter
@Setter
@Entity(name = "page")
public class PageEntity implements Comparable<PageEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    SiteEntity site;

    String path;

    @Column(name = "code")
    int responseCode;

    String content;

    public PageEntity(SiteEntity site, String path, String content, int responseCode) {
        this.site = site;
        this.path = path;
        this.content = content;
        this.responseCode = responseCode;
    }

    public PageEntity() {
    }

    @Override
    public int compareTo(PageEntity o) {
        int x = compare(o.getSite().getId(), this.getSite().getId());
        int y = this.path.compareTo(o.path);

        return Integer.compare(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageEntity page = (PageEntity) o;
        return site.equals(page.site) && path.equals(page.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, path);
    }

    @Override
    public String toString() {
        return "PageEntity{" +
                "id=" + id +
                ", site=" + site +
                ", path='" + path + '\'' +
                ", responseCode=" + responseCode +
                '}';
    }
}
