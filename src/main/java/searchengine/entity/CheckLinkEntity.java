package searchengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

import static java.lang.Integer.compare;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "link")
public class CheckLinkEntity implements Comparable<CheckLinkEntity> {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    @Column(name = "path")
    private String path;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    private SiteEntity site;

    public CheckLinkEntity(String path, SiteEntity site) {
        this.path = path;
        this.site = site;
    }

    @Override
    public int compareTo(CheckLinkEntity o) {
        int x = compare(this.getSite().getId(), o.getSite().getId());
        int y = this.path.compareTo(o.path);

        return Integer.compare(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckLinkEntity that = (CheckLinkEntity) o;
        return path.equals(that.path) && site.equals(that.site);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, site);
    }
}
