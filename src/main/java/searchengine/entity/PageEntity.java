package searchengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

import static java.lang.Integer.compare;

@Getter
@Setter
@Builder
@ToString
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
public class PageEntity {
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

}
