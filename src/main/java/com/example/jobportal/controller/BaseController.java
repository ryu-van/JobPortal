package com.example.jobportal.controller;

import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.PageInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public abstract class BaseController {
    //OK - Success (Have Data)
    protected  <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    //OK - Success (Data + Message)
    protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
    //OK - Success (No Data, Have Message)
    protected <T> ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }
    //OK - Success (Have pagination data)
    protected <T> ResponseEntity<ApiResponse<List<T>>> ok(String message, List<T> data, PageInfo pageInfo) {
        return ResponseEntity.ok(ApiResponse.success(message, data, pageInfo));
    }
    //Created - Success (Have Data)
    protected <T> ResponseEntity<ApiResponse<T>> created(String message,T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, data));
    }
    //Bad Request - (400)
    protected <T> ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }
    // Validation Error - (400)
    protected <T> ResponseEntity<ApiResponse<Void>> validationError(String message,List<String> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message,errors));
    }
    // Not Found - (404)
    protected <T> ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message));
    }
    // Internal Server Error - (500)
    protected <T> ResponseEntity<ApiResponse<Void>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message));
    }
    protected <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message));
    }

    protected <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(message));
    }
}
