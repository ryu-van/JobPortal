package com.example.jobportal.repository;

import com.example.jobportal.dto.response.TopHrDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of custom repository methods for User entity.
 * Uses EntityManager for native SQL queries with proper parameter binding.
 */
@Slf4j
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public long countAllActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users " +
                    "WHERE is_active = true";
        
        Query query = entityManager.createNativeQuery(sql);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countNewUsersInPeriod(LocalDateTime startDate) {
        String sql = "SELECT COUNT(*) FROM users " +
                    "WHERE created_at >= :startDate";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public List<TopHrDto> findTopHrByJobCount(Long companyId, int limit) {
        String sql = "SELECT u.id, u.full_name, COUNT(j.id) as jobs_created " +
                    "FROM users u " +
                    "INNER JOIN roles r ON u.role_id = r.id " +
                    "LEFT JOIN jobs j ON u.id = j.created_by " +
                    "WHERE u.company_id = :companyId " +
                    "AND r.name = 'HR' " +
                    "AND u.is_active = true " +
                    "GROUP BY u.id, u.full_name " +
                    "ORDER BY jobs_created DESC " +
                    "LIMIT :limit";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        query.setParameter("limit", limit);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        return results.stream()
                .map(row -> TopHrDto.builder()
                        .userId(((Number) row[0]).longValue())
                        .fullName((String) row[1])
                        .jobsCreated(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
}
