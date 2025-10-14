package com.example.jobportal.repository;

import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @Query("""
                SELECT new com.example.jobportal.dto.response.JobBaseResponse(
                    j.id,
                    j.title,
                    c.name,
                    j.address.city,
                    c.logoUrl,
                    j.isSalaryNegotiable,
                    j.salaryMin,
                    j.salaryMax,
                    j.salaryCurrency
                )
                FROM Job j
                JOIN j.company c
                JOIN j.categories cat
                WHERE (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:category IS NULL OR LOWER(cat.name) = LOWER(:category))
                  AND (:location IS NULL OR LOWER(j.address.city) LIKE LOWER(CONCAT('%', :location, '%')))
                  AND j.status = 'published'
                ORDER BY j.isFeatured DESC, j.publishedAt DESC
            """)
    Page<JobBaseResponse> getBaseJobs(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            Pageable pageable
    );

}