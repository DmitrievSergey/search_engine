package searchengine.dto.exception;

public abstract class AbstractCustomException extends RuntimeException {
    public static final String PAGE_UNREACHABLE = "Cтраница недоступна";
    private final int statusCode;
    private final String url;


    public AbstractCustomException(String message, int statusCode, String url) {
        super(message + ". Status=" + statusCode + ", URL=[" + url + "]");
        this.statusCode = statusCode;
        this.url = url;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getUrl() {
        return this.url;
    }
}
