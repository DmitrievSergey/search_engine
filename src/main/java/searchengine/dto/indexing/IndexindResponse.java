package searchengine.dto.indexing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class IndexindResponse {
    public static final String INDEXING_ALREADY_BEGIN = "Индексация уже запущена";
    public static final String PAGE_OUT_OF_SITES_CONFIG = "Данная страница находится за пределами сайтов, \n" +
            "указанных в конфигурационном файле\n";
    public static final String INDEXING_NOT_BEGIN = "Индексация не запущена";
    public static final String INDEXING_FAILED = "Индексация не запущена";
    public static final String INDEXING_INTERRUPTED_BY_USER = "Индексация прервана пользователем";
    private boolean result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public IndexindResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
