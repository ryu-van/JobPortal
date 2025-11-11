package com.example.jobportal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private PageInfo pagination;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, List<String> errors) {
        this.success = success;
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
    //Success ressponse with data
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null, null, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null, LocalDateTime.now());
    }
    // Success response with pagination
    public static <T> ApiResponse<List<T>> success(String message, List<T> data, PageInfo pagination) {
        return new ApiResponse<>(true, message, data, null, pagination, LocalDateTime.now());
    }
    // Success response without data for create, update, delete
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, null, LocalDateTime.now());
    }
    // Error response
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null, null, LocalDateTime.now());
    }
    //Error response với multiple errors
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return new ApiResponse<>(false, message, null, errors, null, LocalDateTime.now());
    }
    //Error response với data
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, null, null, LocalDateTime.now());
    }


}
