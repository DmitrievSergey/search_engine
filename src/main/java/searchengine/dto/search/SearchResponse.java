package searchengine.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import searchengine.dto.statistics.SearchStatistic;

import java.util.List;
@Getter
@Setter
public class SearchResponse  {
    public static final String EMPTY_REQUEST = "Введен пустой запрос";
    public static final String NOT_FOUND_PAGE = "Указанной страницы не существует";
    public static final String EMPTY_RESULT = "По запросу : \"%s\" найдено 0 результатов";
    private boolean result = true;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer count;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SearchStatistic> data;
    private String error;

    public SearchResponse() {
    }

    public SearchResponse(boolean result, String error){
        this.error = error;
        this.result = result;
    }

    public SearchResponse(boolean result, int count, List<SearchStatistic> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }

}
