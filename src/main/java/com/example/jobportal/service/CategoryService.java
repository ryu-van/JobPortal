package com.example.jobportal.service;

import com.example.jobportal.dto.request.JobCategoryRequest;
import com.example.jobportal.dto.response.JobCategoryResponse;
import com.example.jobportal.model.entity.JobCategory;

import java.util.List;

public interface CategoryService {
    List<JobCategoryResponse> getListOfCategories(String name);
    JobCategoryResponse getJobCategory(Long id);
    JobCategoryResponse createJobCategory(JobCategoryRequest jobCategoryRequest);
    JobCategoryResponse updateJobCategory(Long id, JobCategoryRequest jobCategoryRequest);
    void deleteJobCategory(Long id);


}
