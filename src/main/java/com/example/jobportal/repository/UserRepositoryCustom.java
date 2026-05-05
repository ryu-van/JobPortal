package com.example.jobportal.repository;

import com.example.jobportal.dto.response.TopHrDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom repository interface for User entity with specialized query methods
 * for dashboard statistics and analytics.
 * 
 * These methods provide optimized aggregation queries that are not easily
 * expressed using Spring Data JPA's standard query derivation.
 */
public interface UserRepositoryCustom {
    
    /**
     * Count all active users in the system (admin)
     * 
     * @return count of users where is_active = true
     */
    long countAllActiveUsers();
    
    /**
     * Count new users created within a time period (admin)
     * 
     * @param startDate Start date of the period (inclusive)
     * @return count of users created on or after startDate
     */
    long countNewUsersInPeriod(LocalDateTime startDate);
    
    /**
     * Find top N HR users by job creation count for a company
     * 
     * @param companyId ID of the company
     * @param limit Maximum number of results to return
     * @return list of TopHrDto ordered by jobs created descending
     */
    List<TopHrDto> findTopHrByJobCount(Long companyId, int limit);
}
