package com.example.jobportal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.jobportal.dto.response.CompanyBaseResponse;
import com.example.jobportal.model.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("""
        SELECT new com.example.jobportal.dto.response.CompanyBaseResponse(
            c.id,
            c.name,
            c.email,
            c.isVerified,
            c.isActive,
            i.name,
            c.companySize,
            c.logoUrl,
            a.provinceName,
            c.createdAt
        )
        FROM Company c
        LEFT JOIN c.industry i
        LEFT JOIN c.addresses a ON a.isPrimary = true
        WHERE (:keyword IS NULL OR 
               LOWER(c.name) LIKE :keyword
               OR LOWER(c.email) LIKE :keyword)
          AND (:location IS NULL OR 
               LOWER(a.provinceName) LIKE :location)
          AND (:isActive IS NULL OR c.isActive = :isActive)
        ORDER BY c.createdAt DESC
    """)
    Page<CompanyBaseResponse> getAllCompanies(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("""
        SELECT new com.example.jobportal.dto.response.CompanyBaseResponse(
            c.id,
            c.name,
            c.email,
            c.isVerified,
            c.isActive,
            i.name,
            c.companySize,
            c.logoUrl,
            a.provinceName,
            c.createdAt
        )
        FROM Company c
        LEFT JOIN c.industry i
        LEFT JOIN c.addresses a ON a.isPrimary = true
        WHERE (
            :keyword IS NULL
            OR LOWER(c.name) LIKE :keyword
            OR LOWER(c.email) LIKE :keyword
        )
        ORDER BY c.createdAt DESC
    """)
    List<CompanyBaseResponse> getListCompany(@Param("keyword") String keyword);
}
