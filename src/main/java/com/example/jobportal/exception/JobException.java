package com.example.jobportal.exception;

import org.springframework.http.HttpStatus;

public class JobException extends RuntimeException {
    private final HttpStatus status;

    public JobException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static JobException notFound(String message) {
        return new JobException(message, HttpStatus.NOT_FOUND);
    }

    public static JobException badRequest(String message) {
        return new JobException(message, HttpStatus.BAD_REQUEST);
    }

    public static JobException forbidden(String message) {
        return new JobException(message, HttpStatus.FORBIDDEN);
    }
}
