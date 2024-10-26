package searchengine.dto.exception;

import lombok.Getter;

import java.util.Optional;

@Getter
public class CustomInfoException extends AbstractCustomException{

    public CustomInfoException(String message, int statusCode, String url) {
        super(message, statusCode, url);
    }
}
