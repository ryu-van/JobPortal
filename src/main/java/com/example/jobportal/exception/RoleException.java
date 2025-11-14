package com.example.jobportal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Setter
@Getter
public class RoleException extends RuntimeException {
    private final HttpStatus status;

    public RoleException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public static RoleException notFound(String message) {
        return new RoleException(message, HttpStatus.NOT_FOUND);
    }

    public static RoleException badRequest(String message) {
        return new RoleException(message, HttpStatus.BAD_REQUEST);
    }

    public static RoleException forbidden(String message) {
        return new RoleException(message, HttpStatus.FORBIDDEN);
    }
}
