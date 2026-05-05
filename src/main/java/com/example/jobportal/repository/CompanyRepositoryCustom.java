package com.example.jobportal.repository;

import java.time.LocalDateTime;

/**
 * Custom repository interface for Company entity with specialized query methods
 * for dashboard statistics and analytics.
 * 
 * These methods provide optimized aggregation queries that are not easily
 * expressed using Spring Data JPA's standard query derivation.
 */
public interface CompanyRepositoryCustom {
    
    /**
     * Count all active companies in the system (admin)
     * 
     * @return count of companies where is_active = true
     */
    long countAllActiveCompanies();
    
    /**
     * Count verified companies in the system (admin)
     * 
     * @return count of companies where is_verified = true and is_active = true
     */
    long countVerifiedCompanies();
    
    /**
     * Count new companies created within a time period (admin)
     * 
     * @param startDate Start date of the period (inclusive)
     * @return count of companies created on or after startDate where is_active = true
     */
    long countNewCompaniesInPeriod(LocalDateTime startDate);
}
