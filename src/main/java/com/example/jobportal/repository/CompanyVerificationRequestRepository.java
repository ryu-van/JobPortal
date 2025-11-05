package com.example.jobportal.repository;

import com.example.jobportal.dto.response.CompanyVerificationRequestResponse;
import com.example.jobportal.model.entity.CompanyVerificationRequest;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;

@Repository
public interface CompanyVerificationRequestRepository extends JpaRepository<CompanyVerificationRequest, Long> {
    CompanyVerificationRequest findByCompanyId(Long companyId);

    boolean existsByUserAndStatus(User sender, CompanyVerificationStatus companyVerificationStatus);
    @Query("""
    SELECT new com.example.jobportal.dto.response.CompanyVerificationRequestResponse(
        re.id,
        re.companyName,
        re.contactEmail,
        CONCAT(re.address.street, ', ', re.address.ward, ', ', re.address.district, ', ', re.address.city),
        re.status,
        re.user.fullName,
        re.createdAt
    )
    FROM CompanyVerificationRequest re
    WHERE (:keyword IS NULL OR LOWER(re.contactEmail) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:verifyStatus IS NULL OR re.status = :verifyStatus)
      AND (:createdAt IS NULL OR re.createdAt >= :createdAt)
    ORDER BY re.createdAt DESC
""")
    Page<CompanyVerificationRequestResponse> getAllCompanyVerificationRequest(
            @Param("keyword") String keyword,
            @Param("verifyStatus") CompanyVerificationStatus verifyStatus,
            @Param("createdAt") LocalDate createdAt,
            Pageable pageable);

}
