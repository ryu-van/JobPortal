package com.example.jobportal.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidPeriodException extends RuntimeException {
    private final HttpStatus status;

    public InvalidPeriodException(String period) {
        super("Invalid time period: " + period);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public InvalidPeriodException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static InvalidPeriodException badRequest(String period) {
        return new InvalidPeriodException(period);
    }
}
