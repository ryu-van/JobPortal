package com.example.jobportal.repository;

import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobBaseResponseV2;
import com.example.jobportal.model.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
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
                  AND (:status IS NULL OR LOWER(j.status) LIKE LOWER(CONCAT('%', :status, '%')))
                              AND j.createdBy.id = :hrId
                ORDER BY j.isFeatured DESC, j.publishedAt DESC
            """)
    Page<JobBaseResponse> getJobsByHrId(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            @Param("status") String status,
            @Param("hrId") Long hrId,
            Pageable pageable
    );

    @Query("""
                SELECT new com.example.jobportal.dto.response.JobBaseResponse(
                    j.id,
                    j.title,
                    c.name,
                    j.address.street,
                    j.address.ward,
                    j.address.district,
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
                  AND (:status IS NULL OR LOWER(j.status) LIKE LOWER(CONCAT('%', :status, '%')))
                  AND j.company.id = :companyId
                ORDER BY j.isFeatured DESC, j.publishedAt DESC
            """)
    Page<JobBaseResponse> getJobsByCompanyId(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(value = """
                SELECT 
                    j.id,
                    j.title,
                    c.name AS company_name,
                    j.street AS street,
                    j.ward AS ward,
                    j.district AS district,
                    j.city AS city,
                    j.country AS country,
                    c.logo_url AS company_logo,
                    j.is_salary_negotiable,
                    j.salary_min,
                    j.salary_max,
                    j.salary_currency,
                    j.work_type,
                    j.employment_type,
                    j.experience_level,
                    j.number_of_positions,
                    j.application_deadline,
                    STRING_AGG(cat.name, ', ') AS category_names
                FROM jobs j
                JOIN companies c ON j.company_id = c.id
                LEFT JOIN job_category_mapping jcm ON j.id = jcm.job_id
                LEFT JOIN job_categories cat ON jcm.category_id = cat.id
                WHERE (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:category IS NULL OR LOWER(cat.name) = LOWER(:category))
                  AND (:location IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :location, '%')))
                  AND j.status = 'PUBLISHED'
                GROUP BY j.id, j.title, c.name, a.address, c.logo_url,
                         j.is_salary_negotiable, j.salary_min, j.salary_max, j.salary_currency,
                         j.work_type, j.employment_type, j.experience_level,
                         j.number_of_positions, j.application_deadline
                ORDER BY j.is_featured DESC, j.published_at DESC
            """,
            countQuery = """
                        SELECT COUNT(DISTINCT j.id)
                        FROM jobs j
                        JOIN companies c ON j.company_id = c.id
                        LEFT JOIN job_category_mapping jcm ON j.id = jcm.job_id
                        LEFT JOIN job_categories cat ON jcm.category_id = cat.id
                        LEFT JOIN base_address a ON j.address_id = a.id
                        WHERE (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                          AND (:category IS NULL OR LOWER(cat.name) = LOWER(:category))
                          AND (:location IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :location, '%')))
                          AND j.status = 'PUBLISHED'
                    """,
            nativeQuery = true)
    Page<Object[]> getJobsNative(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            Pageable pageable
    );


}