package com.example.jobportal.repository;

import com.example.jobportal.dto.response.TopJobDto;
import com.example.jobportal.model.enums.JobStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of custom repository methods for Job entity.
 * Uses EntityManager for native SQL queries with proper parameter binding.
 */
@Slf4j
public class JobRepositoryCustomImpl implements JobRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public long countPublishedJobsByCompanyId(Long companyId) {
        String sql = "SELECT COUNT(*) FROM jobs " +
                    "WHERE company_id = :companyId " +
                    "AND status = 'published'";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countPublishedJobsByCompanyIdAndPeriod(Long companyId, LocalDateTime startDate) {
        String sql = "SELECT COUNT(*) FROM jobs " +
                    "WHERE company_id = :companyId " +
                    "AND status = 'published' " +
                    "AND created_at >= :startDate";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        query.setParameter("startDate", startDate);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countAllPublishedJobs() {
        String sql = "SELECT COUNT(*) FROM jobs " +
                    "WHERE status = 'published'";
        
        Query query = entityManager.createNativeQuery(sql);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countNewJobsInPeriod(LocalDateTime startDate) {
        String sql = "SELECT COUNT(*) FROM jobs " +
                    "WHERE created_at >= :startDate";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long sumViewsByCompanyId(Long companyId) {
        String sql = "SELECT COALESCE(SUM(views_count), 0) FROM jobs " +
                    "WHERE company_id = :companyId";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public List<TopJobDto> findTopJobsByApplicationCount(Long companyId, int limit) {
        String sql = "SELECT j.id, j.title, COUNT(a.id) as app_count, j.published_at " +
                    "FROM jobs j " +
                    "LEFT JOIN applications a ON j.id = a.job_id " +
                    "WHERE j.company_id = :companyId " +
                    "AND j.status = 'published' " +
                    "GROUP BY j.id, j.title, j.published_at " +
                    "ORDER BY app_count DESC " +
                    "LIMIT :limit";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        query.setParameter("limit", limit);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        return results.stream()
                .map(row -> TopJobDto.builder()
                        .jobId(((Number) row[0]).longValue())
                        .jobTitle((String) row[1])
                        .applicationCount(((Number) row[2]).longValue())
                        .publishedAt(row[3] != null ? (LocalDateTime) row[3] : null)
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<JobStatus, Long> countJobsByStatusForCompany(Long companyId) {
        String sql = "SELECT status, COUNT(*) as count " +
                    "FROM jobs " +
                    "WHERE company_id = :companyId " +
                    "GROUP BY status";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        Map<JobStatus, Long> statusCounts = new EnumMap<>(JobStatus.class);
        
        // Initialize all statuses with 0
        for (JobStatus status : JobStatus.values()) {
            statusCounts.put(status, 0L);
        }
        
        // Populate with actual counts
        for (Object[] row : results) {
            String statusValue = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            
            try {
                JobStatus status = JobStatus.fromValue(statusValue);
                statusCounts.put(status, count);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown job status value in database: {}", statusValue);
            }
        }
        
        return statusCounts;
    }
    
    @Override
    public Double calculateAvgTimeToFirstApplication(Long companyId) {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (a.min_applied - j.published_at)) / 3600) as avg_hours " +
                    "FROM jobs j " +
                    "INNER JOIN ( " +
                    "    SELECT job_id, MIN(applied_at) as min_applied " +
                    "    FROM applications " +
                    "    GROUP BY job_id " +
                    ") a ON j.id = a.job_id " +
                    "WHERE j.company_id = :companyId " +
                    "AND j.published_at IS NOT NULL";
        
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
