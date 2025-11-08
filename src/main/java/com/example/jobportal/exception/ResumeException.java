package com.example.jobportal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Setter
@Getter
public class ResumeException extends RuntimeException{
    private final HttpStatus status;

    public ResumeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public static ResumeException notFound(String message) {
        return new ResumeException(message, HttpStatus.NOT_FOUND);
    }

    public static ResumeException badRequest(String message) {
        return new ResumeException(message, HttpStatus.BAD_REQUEST);
    }

    public static ResumeException forbidden(String message) {
        return new ResumeException(message, HttpStatus.FORBIDDEN);
    }
}
