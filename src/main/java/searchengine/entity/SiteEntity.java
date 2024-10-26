package searchengine.entity;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.compare;


@Entity
@Table(name = "site")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SiteEntity{

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Integer id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    Status status;

    @Column(name = "status_time")
    LocalDateTime statusTime;

    @Column(name = "last_error")
    String lastError;

    String url;

    String name;

//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "site", cascade = CascadeType.ALL)
//    protected List<PageEntity> pageList = new ArrayList<>();

    public SiteEntity(Status status, LocalDateTime statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }

    @Override
    public String toString() {
        return "SiteEntity{" +
                "id=" + id +
                ", status=" + status +
                ", statusTime=" + statusTime +
                ", lastError='" + lastError + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
