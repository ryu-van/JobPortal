package com.example.jobportal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Setter
@Getter
public class ApplicationException extends RuntimeException {
    private final HttpStatus status;

    public ApplicationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public static ApplicationException notFound(String message) {
        return new ApplicationException(message, HttpStatus.NOT_FOUND);
    }

    public static ApplicationException badRequest(String message) {
        return new ApplicationException(message, HttpStatus.BAD_REQUEST);
    }

    public static ApplicationException forbidden(String message) {
        return new ApplicationException(message, HttpStatus.FORBIDDEN);
    }
}
