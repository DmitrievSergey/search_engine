package searchengine.dto.exception;

public class CustomInterruptException extends AbstractCustomException{

    public CustomInterruptException(String message, int statusCode, String url) {
        super(message, statusCode, url);
    }
}
