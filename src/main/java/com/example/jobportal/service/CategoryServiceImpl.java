package com.example.jobportal.service;

import com.example.jobportal.dto.request.JobCategoryRequest;
import com.example.jobportal.dto.response.JobCategoryResponse;
import com.example.jobportal.exception.JobException;
import com.example.jobportal.model.entity.JobCategory;
import com.example.jobportal.repository.JobCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final JobCategoryRepository jobCategoryRepository;

    @Override
    @Cacheable(cacheNames = "categories", key = "T(java.util.Objects).toString(#name)")
    public List<JobCategoryResponse> getListOfCategories(String name) {
        return jobCategoryRepository.findByNameOrAll(name).stream()
                .map(category -> new JobCategoryResponse().fromEntity(category))
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "categories", key = "'id:'+#id")
    public JobCategoryResponse getJobCategory(Long id) {
        JobCategory jobCategory = jobCategoryRepository.findById(id).orElseThrow(
                () -> JobException.notFound("Job category not found with id: " + id));
        return new JobCategoryResponse().fromEntity(jobCategory);
    }

    @Override
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public JobCategoryResponse createJobCategory(JobCategoryRequest jobCategoryRequest) {
        if (jobCategoryRequest.getName() == null || jobCategoryRequest.getName().isEmpty()) {
            throw JobException.badRequest("Job category name cannot be null or empty");
        }
        if (jobCategoryRequest.getDescription() == null || jobCategoryRequest.getDescription().isEmpty()) {
            throw JobException.badRequest("Job category description cannot be null or empty");
        }
        JobCategory parentCategory = null;
        if (jobCategoryRequest.getParentId() != null) {
            parentCategory = jobCategoryRepository.findById(jobCategoryRequest.getParentId())
                    .orElseThrow(() -> JobException.notFound("Parent category not found with id: " + jobCategoryRequest.getParentId()));
        }
        JobCategory jobCategory = new JobCategory();
        jobCategory.setName(jobCategoryRequest.getName());
        jobCategory.setDescription(jobCategoryRequest.getDescription());
        jobCategory.setParentCategory(parentCategory);
        return new JobCategoryResponse().fromEntity(jobCategoryRepository.save(jobCategory));
    }

    @Override
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public JobCategoryResponse updateJobCategory(Long id, JobCategoryRequest jobCategoryRequest) {
        JobCategory existingCategory = jobCategoryRepository.findById(id)
                .orElseThrow(() -> JobException.notFound("Job category not found with id: " + id));
        for (JobCategory category : jobCategoryRepository.findAll()) {
            if (jobCategoryRequest.getName().equals(category.getName())) {
                throw JobException.notFound("Job category already exists with name: " + category.getName());
            }
        }
        existingCategory.setName(jobCategoryRequest.getName());
        existingCategory.setDescription(jobCategoryRequest.getDescription());
        return new JobCategoryResponse().fromEntity(jobCategoryRepository.save(existingCategory));
    }

    @Override
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void deleteJobCategory(Long id) {
        JobCategory existingCategory = jobCategoryRepository.findById(id)
                .orElseThrow(() -> JobException.notFound("Job category not found with id: " + id));
        jobCategoryRepository.delete(existingCategory);
    }
}
