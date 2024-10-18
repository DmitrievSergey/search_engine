package searchengine.entity;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.Objects;

import static java.lang.Integer.compare;


@Entity
@Table(name = "site")
@Getter
@Setter
@Builder
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


//    public SiteEntity(Status status, LocalDateTime statusTime, String lastError, String url, String name) {
//        this.status = status;
//        this.statusTime = statusTime;
//        this.lastError = lastError;
//        this.url = url;
//        this.name = name;
//    }
//
//    @Override
//    public int compareTo(SiteEntity o) {
//
//        int x = compare(o.getId(), this.getId());
//        int y = this.getUrl().compareTo(o.getUrl());
//
//        return Integer.compare(x, y);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        SiteEntity that = (SiteEntity) o;
//        return url.equals(that.url) && name.equals(that.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(url, name);
//    }
//
//    @Override
//    public String toString() {
//        return "SiteEntity{" +
//                "id=" + id +
//                ", status=" + status +
//                ", statusTime=" + statusTime +
//                ", lastError='" + lastError + '\'' +
//                ", url='" + url + '\'' +
//                ", name='" + name + '\'' +
//                '}';
//    }
}
