package com.example.jobportal.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.jobportal.dto.response.CompanyVerificationRequestResponse;
import com.example.jobportal.model.entity.CompanyVerificationRequest;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.CompanyVerificationStatus;

@Repository
public interface CompanyVerificationRequestRepository extends JpaRepository<CompanyVerificationRequest, Long> {
    CompanyVerificationRequest findByCompanyId(Long companyId);

    boolean existsByUserAndStatus(User sender, CompanyVerificationStatus companyVerificationStatus);

    @Query("""
                SELECT new com.example.jobportal.dto.response.CompanyVerificationRequestResponse(
                    re.id,
                    re.companyName,
                    re.contactEmail,
                    CONCAT(
                        COALESCE(a.detailAddress, ''),
                        CASE WHEN a.communeName IS NOT NULL 
                             THEN CONCAT(', ', a.communeName) 
                             ELSE '' END,
                        CASE WHEN a.provinceName IS NOT NULL 
                             THEN CONCAT(', ', a.provinceName) 
                             ELSE '' END
                    ),
                    re.status,
                    u.fullName,
                    re.createdAt
                )
                FROM CompanyVerificationRequest re
                LEFT JOIN re.addresses a
                LEFT JOIN re.user u
                WHERE (:keyword IS NULL 
                       OR LOWER(re.contactEmail) LIKE :keyword)
                  AND (:verifyStatus IS NULL OR re.status = :verifyStatus)
                  AND (re.createdAt >= COALESCE(:createdAtStart, re.createdAt))
                ORDER BY re.createdAt DESC
            """)
    Page<CompanyVerificationRequestResponse> getAllCompanyVerificationRequest(
            @Param("keyword") String keyword,
            @Param("verifyStatus") CompanyVerificationStatus verifyStatus,
            @Param("createdAtStart") LocalDateTime createdAtStart,
            Pageable pageable);

}
