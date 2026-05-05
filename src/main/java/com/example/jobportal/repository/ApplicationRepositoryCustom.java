package com.example.jobportal.repository;

import com.example.jobportal.model.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom repository interface for Application entity with specialized query methods
 * for dashboard statistics and analytics.
 * 
 * These methods provide optimized aggregation queries that are not easily
 * expressed using Spring Data JPA's standard query derivation.
 */
public interface ApplicationRepositoryCustom {
    
    /**
     * Count applications by company and status
     * 
     * @param companyId ID of the company
     * @param status Application status to filter by
     * @return count of applications with given status for company's jobs
     */
    long countByCompanyIdAndStatus(Long companyId, ApplicationStatus status);
    
    /**
     * Count all applications for a company
     * 
     * @param companyId ID of the company
     * @return total count of applications for company's jobs
     */
    long countByCompanyId(Long companyId);
    
    /**
     * Count applications by company within a time period
     * 
     * @param companyId ID of the company
     * @param startDate Start date of the period (inclusive)
     * @return count of applications created on or after startDate for company's jobs
     */
    long countByCompanyIdAndPeriod(Long companyId, LocalDateTime startDate);
    
    /**
     * Count applications by status across all companies (admin)
     * 
     * @return map of ApplicationStatus to count
     */
    Map<ApplicationStatus, Long> countAllByStatus();
    
    /**
     * Count all applications in the system (admin)
     * 
     * @return total count of all applications
     */
    long countAllApplications();
    
    /**
     * Count new applications created within a time period (admin)
     * 
     * @param startDate Start date of the period (inclusive)
     * @return count of applications created on or after startDate
     */
    long countNewApplicationsInPeriod(LocalDateTime startDate);
    
    /**
     * Calculate average applications per job for a company
     * 
     * @param companyId ID of the company
     * @return average number of applications per job, or null if no jobs
     */
    Double calculateAvgApplicationsPerJob(Long companyId);
}
