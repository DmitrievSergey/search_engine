package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.dto.IndexDto;
import searchengine.services.search.SearchServiceImpl;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "`index`", indexes = {@Index(
        name = "page_id_list", columnList = "page_id"),
        @Index(name = "lemma_id_list", columnList = "lemma_id")})
public class IndexSearchEntity implements Comparable<IndexSearchEntity> {
    private static Logger logger = LoggerFactory.getLogger(IndexSearchEntity.class);
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
    public int compareTo(IndexSearchEntity o) {
        return this.getPage().compareTo(o.getPage());
    }
}
