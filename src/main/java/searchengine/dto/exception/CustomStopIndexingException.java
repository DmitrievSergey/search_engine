package searchengine.dto.exception;

public class CustomStopIndexingException extends RuntimeException {
    public CustomStopIndexingException(String message, Throwable cause) {
        super(message, cause);
    }
}
