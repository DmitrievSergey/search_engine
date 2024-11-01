package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "lemma", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lemma", "site_id"})
})
public class LemmaEntity implements Comparable<LemmaEntity> {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<IndexSearchEntity> index = new ArrayList<>();


    public LemmaEntity(SiteEntity site, String lemma, int frequency) {
        this.site = site;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(LemmaEntity o) {
        Integer th = this.getFrequency();
        Integer og = o.getFrequency();
        return th.compareTo(og);
    }
}
