package com.example.jobportal.repository;

import com.example.jobportal.model.entity.CompanyInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyInvitationRepository extends JpaRepository<CompanyInvitation, Long> {
    Optional<CompanyInvitation> findByCode(String code);

    boolean existsByCode(String code);

    List<CompanyInvitation> findByCompanyId(Long companyId);

    List<CompanyInvitation> findByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("SELECT i FROM CompanyInvitation i WHERE i.expiresAt < :now")
    List<CompanyInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);

    @Query("SELECT i FROM CompanyInvitation i WHERE i.expiresAt < :now AND i.isActive = TRUE")
    List<CompanyInvitation> findExpiredActiveInvitations(@Param("now") LocalDateTime now);
}
