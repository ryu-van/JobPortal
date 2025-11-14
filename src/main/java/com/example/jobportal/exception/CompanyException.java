package com.example.jobportal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@AllArgsConstructor
@Setter
@Getter
public class CompanyException extends RuntimeException {
    private final HttpStatus status;

    public CompanyException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public static CompanyException notFound(String message) {
        return new CompanyException(message, HttpStatus.NOT_FOUND);
    }

    public static CompanyException badRequest(String message) {
        return new CompanyException(message, HttpStatus.BAD_REQUEST);
    }

    public static CompanyException forbidden(String message) {
        return new CompanyException(message, HttpStatus.FORBIDDEN);
    }

    public static CompanyException illegal(String message) {
        return new CompanyException(message, HttpStatus.CONFLICT);
    }
    public static CompanyException internal(String message) {
        return new CompanyException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
