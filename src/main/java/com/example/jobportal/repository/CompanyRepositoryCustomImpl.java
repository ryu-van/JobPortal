package com.example.jobportal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Implementation of custom repository methods for Company entity.
 * Uses EntityManager for native SQL queries with proper parameter binding.
 */
@Slf4j
public class CompanyRepositoryCustomImpl implements CompanyRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public long countAllActiveCompanies() {
        String sql = "SELECT COUNT(*) FROM companies " +
                    "WHERE is_active = true";
        
        Query query = entityManager.createNativeQuery(sql);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countVerifiedCompanies() {
        String sql = "SELECT COUNT(*) FROM companies " +
                    "WHERE is_active = true " +
                    "AND is_verified = true";
        
        Query query = entityManager.createNativeQuery(sql);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
    
    @Override
    public long countNewCompaniesInPeriod(LocalDateTime startDate) {
        String sql = "SELECT COUNT(*) FROM companies " +
                    "WHERE is_active = true " +
                    "AND created_at >= :startDate";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
}
