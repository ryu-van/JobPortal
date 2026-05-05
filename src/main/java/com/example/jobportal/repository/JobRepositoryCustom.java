package com.example.jobportal.repository;

import com.example.jobportal.dto.response.TopJobDto;
import com.example.jobportal.model.enums.JobStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Custom repository interface for Job entity with specialized query methods
 * for dashboard statistics and analytics.
 * 
 * These methods provide optimized aggregation queries that are not easily
 * expressed using Spring Data JPA's standard query derivation.
 */
public interface JobRepositoryCustom {
    
    /**
     * Count published jobs for a specific company
     * 
     * @param companyId ID of the company
     * @return count of published jobs where is_active = true
     */
    long countPublishedJobsByCompanyId(Long companyId);
    
    /**
     * Count published jobs for a specific company within a time period
     * 
     * @param companyId ID of the company
     * @param startDate Start date of the period (inclusive)
     * @return count of published jobs created on or after startDate
     */
    long countPublishedJobsByCompanyIdAndPeriod(Long companyId, LocalDateTime startDate);
    
    /**
     * Count all published jobs in the system (admin)
     * 
     * @return total count of published jobs where is_active = true
     */
    long countAllPublishedJobs();
    
    /**
     * Count new jobs created within a time period (admin)
     * 
     * @param startDate Start date of the period (inclusive)
     * @return count of jobs created on or after startDate
     */
    long countNewJobsInPeriod(LocalDateTime startDate);
    
    /**
     * Sum total views for all jobs belonging to a company
     * 
     * @param companyId ID of the company
     * @return sum of views_count for all company jobs
     */
    long sumViewsByCompanyId(Long companyId);
    
    /**
     * Find top N jobs by application count for a company
     * 
     * @param companyId ID of the company
     * @param limit Maximum number of results to return
     * @return list of TopJobDto ordered by application count descending
     */
    List<TopJobDto> findTopJobsByApplicationCount(Long companyId, int limit);
    
    /**
     * Count jobs by status for a specific company
     * 
     * @param companyId ID of the company
     * @return map of JobStatus to count
     */
    Map<JobStatus, Long> countJobsByStatusForCompany(Long companyId);
    
    /**
     * Calculate average time (in hours) from job publication to first application
     * for a company's jobs
     * 
     * @param companyId ID of the company
     * @return average hours to first application, or null if no data
     */
    Double calculateAvgTimeToFirstApplication(Long companyId);
}
