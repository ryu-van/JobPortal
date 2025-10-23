package com.example.jobportal.controller;

import com.example.jobportal.dto.request.JobCategoryRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobCategoryResponse;
import com.example.jobportal.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/categories")
public class CategoryController extends BaseController {
    private final CategoryService categoryService;
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<JobCategoryResponse>>> getCategories(@RequestParam String keyword) {
        List<JobCategoryResponse> jobCategoryResponse = categoryService.getListOfCategories(keyword);
        return ok("Get categories successfully", jobCategoryResponse);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobCategoryResponse>> getCategory(@PathVariable Long id) {
        JobCategoryResponse jobCategoryResponse = categoryService.getJobCategory(id);
        return ok("Get category successfully", jobCategoryResponse);
    }
    @PostMapping("/")
    public ResponseEntity<ApiResponse<JobCategoryResponse>> createCategory(@RequestBody JobCategoryRequest jobCategoryRequest) {
        JobCategoryResponse jobCategoryResponse = categoryService.createJobCategory(jobCategoryRequest);
        return ok("Create category successfully", jobCategoryResponse);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobCategoryResponse>> updateCategory(@PathVariable Long id, @RequestBody JobCategoryRequest jobCategoryRequest) {
        JobCategoryResponse jobCategoryResponse = categoryService.updateJobCategory(id, jobCategoryRequest);
        return ok("Update category successfully", jobCategoryResponse);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteJobCategory(id);
        return ok("Delete category successfully", null);
    }

}
