package com.example.jobportal.repository;

import com.example.jobportal.model.entity.JobCategory;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    long countByIdIn(@NotEmpty(message = "At least one category is required") Set<Long> categoryIds);

    Set<JobCategory> findByIdIn(@NotEmpty(message = "At least one category is required") Set<Long> categoryIds);

    @Query("SELECT c FROM JobCategory c WHERE :name IS NULL OR c.name = :name")
    List<JobCategory> findByNameOrAll(@Param("name") String name);
}
