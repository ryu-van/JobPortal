package com.example.jobportal.repository;

import com.example.jobportal.model.enums.ApplicationStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of custom repository methods for Application entity.
 * Uses EntityManager for native SQL queries with proper parameter binding.
 * Joins with jobs table to filter by company_id.
 */
@Slf4j
public class ApplicationRepositoryCustomImpl implements ApplicationRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public long countByCompanyIdAndStatus(Long companyId, ApplicationStatus status) {
        String sql = "SELECT COUNT(a.*) " +
                    "FROM applications a " +
                    "INNER JOIN jobs j ON a.job_id = j.id " +
                    "WHERE j.company_id = :companyId " +
                    "AND a.status = :status";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        query.setParameter("status", status.getValue());
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countByCompanyId(Long companyId) {
        String sql = "SELECT COUNT(a.*) " +
                    "FROM applications a " +
                    "INNER JOIN jobs j ON a.job_id = j.id " +
                    "WHERE j.company_id = :companyId";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countByCompanyIdAndPeriod(Long companyId, LocalDateTime startDate) {
        String sql = "SELECT COUNT(a.*) " +
                    "FROM applications a " +
                    "INNER JOIN jobs j ON a.job_id = j.id " +
                    "WHERE j.company_id = :companyId " +
                    "AND a.applied_at >= :startDate";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        query.setParameter("startDate", startDate);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public Map<ApplicationStatus, Long> countAllByStatus() {
        String sql = "SELECT status, COUNT(*) as count " +
                    "FROM applications " +
                    "GROUP BY status";
        
        Query query = entityManager.createNativeQuery(sql);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        Map<ApplicationStatus, Long> statusCounts = new EnumMap<>(ApplicationStatus.class);
        
        // Initialize all statuses with 0
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusCounts.put(status, 0L);
        }
        
        // Populate with actual counts
        for (Object[] row : results) {
            String statusValue = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            
            try {
                ApplicationStatus status = ApplicationStatus.fromValue(statusValue);
                statusCounts.put(status, count);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown application status value in database: {}", statusValue);
            }
        }
        
        return statusCounts;
    }
    
    @Override
    public long countAllApplications() {
        String sql = "SELECT COUNT(*) FROM applications";
        
        Query query = entityManager.createNativeQuery(sql);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countNewApplicationsInPeriod(LocalDateTime startDate) {
        String sql = "SELECT COUNT(*) FROM applications " +
                    "WHERE applied_at >= :startDate";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public Double calculateAvgApplicationsPerJob(Long companyId) {
        String sql = "SELECT AVG(app_count) " +
                    "FROM ( " +
                    "    SELECT COUNT(a.id) as app_count " +
                    "    FROM jobs j " +
                    "    LEFT JOIN applications a ON j.id = a.job_id " +
                    "    WHERE j.company_id = :companyId " +
                    "    GROUP BY j.id " +
                    ") as job_app_counts";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        
        Object result = query.getSingleResult();
        
        if (result == null) {
            return null;
        }
        
        // Handle different numeric types that might be returned
        if (result instanceof BigDecimal) {
            return ((BigDecimal) result).doubleValue();
        } else if (result instanceof Double) {
            return (Double) result;
        } else if (result instanceof Number) {
            return ((Number) result).doubleValue();
        }
        
        return null;
    }
}
