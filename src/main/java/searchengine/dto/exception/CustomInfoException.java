package searchengine.dto.exception;

import lombok.Getter;

import java.util.Optional;

@Getter
public class CustomInfoException extends RuntimeException{
    private final int statusCode;
    private final String url;

    public CustomInfoException(String message, int statusCode, String url) {
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
