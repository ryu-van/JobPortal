package com.example.jobportal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.jobportal.model.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query(value = """
        SELECT DISTINCT j.*
        FROM jobs j
        JOIN job_category_mapping jcm ON j.id = jcm.job_id
        JOIN job_categories cat ON cat.id = jcm.category_id
        LEFT JOIN addresses a ON a.id = j.address_id
        WHERE (:keyword IS NULL OR j.title ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
          AND (:category IS NULL OR LOWER(cat.name) = LOWER(CAST(:category AS TEXT)))
          AND (:location IS NULL OR a.province_name ILIKE CONCAT('%', CAST(:location AS TEXT), '%'))
          AND j.status = 'published'
        ORDER BY j.is_featured DESC, j.published_at DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT j.id)
        FROM jobs j
        JOIN job_category_mapping jcm ON j.id = jcm.job_id
        JOIN job_categories cat ON cat.id = jcm.category_id
        LEFT JOIN addresses a ON a.id = j.address_id
        WHERE (:keyword IS NULL OR j.title ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
          AND (:category IS NULL OR LOWER(cat.name) = LOWER(CAST(:category AS TEXT)))
          AND (:location IS NULL OR a.province_name ILIKE CONCAT('%', CAST(:location AS TEXT), '%'))
          AND j.status = 'published'
    """,
            nativeQuery = true)
    Page<Job> getBaseJobs(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            Pageable pageable
    );


    @Query(value = """
        SELECT DISTINCT j.*
        FROM jobs j
        JOIN job_category_mapping jcm ON j.id = jcm.job_id
        JOIN job_categories cat ON cat.id = jcm.category_id
        LEFT JOIN addresses a ON a.id = j.address_id
        WHERE (:keyword IS NULL OR j.title ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
          AND (:category IS NULL OR LOWER(cat.name) = LOWER(CAST(:category AS TEXT)))
          AND (:location IS NULL OR a.province_name ILIKE CONCAT('%', CAST(:location AS TEXT), '%'))
          AND (:status IS NULL OR j.status ILIKE CONCAT('%', CAST(:status AS TEXT), '%'))
          AND j.created_by = :hrId
        ORDER BY j.is_featured DESC, j.published_at DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT j.id)
        FROM jobs j
        JOIN job_category_mapping jcm ON j.id = jcm.job_id
        JOIN job_categories cat ON cat.id = jcm.category_id
        LEFT JOIN addresses a ON a.id = j.address_id
        WHERE (:keyword IS NULL OR j.title ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
          AND (:category IS NULL OR LOWER(cat.name) = LOWER(CAST(:category AS TEXT)))
          AND (:location IS NULL OR a.province_name ILIKE CONCAT('%', CAST(:location AS TEXT), '%'))
          AND (:status IS NULL OR j.status ILIKE CONCAT('%', CAST(:status AS TEXT), '%'))
          AND j.created_by = :hrId
    """,
            nativeQuery = true)
    Page<Job> getJobsByHrId(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            @Param("status") String status,
            @Param("hrId") Long hrId,
            Pageable pageable
    );

    @Query(value = """
        SELECT DISTINCT j.*
        FROM jobs j
        JOIN job_category_mapping jcm ON j.id = jcm.job_id
        JOIN job_categories cat ON cat.id = jcm.category_id
        LEFT JOIN addresses a ON a.id = j.address_id
        WHERE (:keyword IS NULL OR j.title ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
          AND (:category IS NULL OR LOWER(cat.name) = LOWER(CAST(:category AS TEXT)))
          AND (:location IS NULL OR a.province_name ILIKE CONCAT('%', CAST(:location AS TEXT), '%'))
          AND (:status IS NULL OR j.status ILIKE CONCAT('%', CAST(:status AS TEXT), '%'))
          AND j.company_id = :companyId
        ORDER BY j.is_featured DESC, j.published_at DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT j.id)
        FROM jobs j
        JOIN job_category_mapping jcm ON j.id = jcm.job_id
        JOIN job_categories cat ON cat.id = jcm.category_id
        LEFT JOIN addresses a ON a.id = j.address_id
        WHERE (:keyword IS NULL OR j.title ILIKE CONCAT('%', CAST(:keyword AS TEXT), '%'))
          AND (:category IS NULL OR LOWER(cat.name) = LOWER(CAST(:category AS TEXT)))
          AND (:location IS NULL OR a.province_name ILIKE CONCAT('%', CAST(:location AS TEXT), '%'))
          AND (:status IS NULL OR j.status ILIKE CONCAT('%', CAST(:status AS TEXT), '%'))
          AND j.company_id = :companyId
    """,
            nativeQuery = true)
    Page<Job> getJobsByCompanyId(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            @Param("status") String status,
            @Param("companyId") Long companyId,
            Pageable pageable
    );

    // Native query giữ nguyên
    @Query(value = """
        SELECT 
            j.id,
            j.title,
            c.name,
            c.logo_url,
            j.is_salary_negotiable,
            j.salary_min,
            j.salary_max,
            j.salary_currency,
            j.work_type,
            j.employment_type,
            j.experience_level,
            j.number_of_positions,
            j.application_deadline,
            STRING_AGG(cat.name, ', ')
        FROM jobs j
        JOIN companies c ON j.company_id = c.id
        LEFT JOIN job_category_mapping jcm ON j.id = jcm.job_id
        LEFT JOIN job_categories cat ON jcm.category_id = cat.id
        WHERE (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:category IS NULL OR LOWER(cat.name) = LOWER(:category))
          AND j.status = 'PUBLISHED'
        GROUP BY j.id, j.title, c.name, c.logo_url,
                 j.is_salary_negotiable, j.salary_min, j.salary_max,
                 j.salary_currency, j.work_type, j.employment_type,
                 j.experience_level, j.number_of_positions, j.application_deadline
        ORDER BY j.id DESC
    """,
            countQuery = "SELECT COUNT(DISTINCT j.id) FROM jobs j",
            nativeQuery = true)
    Page<Object[]> getJobsNative(
            @Param("keyword") String keyword,
            @Param("category") String category,
            Pageable pageable
    );
}
