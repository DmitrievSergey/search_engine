package searchengine.dto.search;

import lombok.*;

import java.util.List;
@Data
public class SearchDto {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
}
