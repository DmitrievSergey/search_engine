package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lemma", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lemma", "site_id"})
})
public class LemmaEntity {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LemmaEntity lemma1 = (LemmaEntity) o;
        return Objects.equals(site, lemma1.site) && Objects.equals(lemma, lemma1.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, lemma);
    }
}
