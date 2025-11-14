package com.example.jobportal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Setter
@Getter
public class UserException extends RuntimeException {
    private final HttpStatus status;

    public UserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public static UserException notFound(String message) {
        return new UserException(message, HttpStatus.NOT_FOUND);
    }

    public static UserException badRequest(String message) {
        return new UserException(message, HttpStatus.BAD_REQUEST);
    }

    public static UserException forbidden(String message) {
        return new UserException(message, HttpStatus.FORBIDDEN);
    }

    public static UserException illegal(String message) {
        return new UserException(message, HttpStatus.CONFLICT);
    }


}
