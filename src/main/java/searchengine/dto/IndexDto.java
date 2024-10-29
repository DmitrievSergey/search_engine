package searchengine.dto;

import lombok.Getter;
import lombok.Setter;


import lombok.Value;
import searchengine.entity.IndexSearchEntity;

import java.util.Objects;
@Getter
@Setter
public class IndexDto implements Comparable<IndexDto> {
    int pageId;
    int lemmaId;
    int rank;

    public IndexDto() {
    }

    public IndexDto(int pageId, int lemmaId, int rank) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexDto indexDto = (IndexDto) o;
        return pageId == indexDto.pageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageId);
    }

    @Override
    public int compareTo(IndexDto o) {
        Integer thisPage = this.getPageId();
        Integer oPage = o.getPageId();
        return thisPage.compareTo(oPage);
    }
}
