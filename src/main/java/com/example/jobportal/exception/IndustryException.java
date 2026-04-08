package com.example.jobportal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Setter
@Getter
public class IndustryException extends RuntimeException{
    private final HttpStatus status;

    public IndustryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public static IndustryException notFound(String message) {
        return new IndustryException(message, HttpStatus.NOT_FOUND);
    }

    public static IndustryException badRequest(String message) {
        return new IndustryException(message, HttpStatus.BAD_REQUEST);
    }

    public static IndustryException forbidden(String message) {
        return new IndustryException(message, HttpStatus.FORBIDDEN);
    }
}
