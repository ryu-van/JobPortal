package com.example.jobportal.repository;

import com.example.jobportal.model.entity.JobCategory;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    long countByIdIn(@NotEmpty(message = "At least one category is required") Set<Long> categoryIds);

    Set<JobCategory> findByIdIn(@NotEmpty(message = "At least one category is required") Set<Long> categoryIds);
}
