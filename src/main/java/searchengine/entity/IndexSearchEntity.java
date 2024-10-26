package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "`index`", indexes = {@Index(
        name = "page_id_list", columnList = "page_id"),
        @Index(name = "lemma_id_list", columnList = "lemma_id")})
public class IndexSearchEntity implements Serializable {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false)
    private LemmaEntity lemma;

    @Column(name = "`rank`", nullable = false)
    private int rank;

    public IndexSearchEntity(PageEntity page, LemmaEntity lemma, int rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    public IndexSearchEntity() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexSearchEntity that = (IndexSearchEntity) o;
        return page.equals(that.page) && lemma.equals(that.lemma) && rank == that.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, lemma, rank);
    }
}
