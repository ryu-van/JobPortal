package com.example.jobportal.service;

import com.example.jobportal.dto.request.NewCompanyVerificationRequest;
import com.example.jobportal.dto.request.UpdateCompanyRequest;
import com.example.jobportal.dto.response.CompanyBaseResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestDetailResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestResponse;
import com.example.jobportal.dto.response.InvitationResponse;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.CompanyInvitation;
import com.example.jobportal.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CompanyService {

    Page<CompanyVerificationRequestResponse> getAllCompanyVerificationRequest(String keyword, String verifyStatus, LocalDate createdDate, Pageable pageable);
    Page<CompanyBaseResponse> getAllCompanies(String keyword, String location, Boolean isActive, Pageable pageable);
    CompanyBaseResponse updateCompany(Long companyId, UpdateCompanyRequest companyRequest);
    CompanyBaseResponse getCompanyById(Long companyId);
    CompanyVerificationRequestResponse  createCompanyVerificationRequest(NewCompanyVerificationRequest newCompanyVerificationRequest);
    CompanyVerificationRequestDetailResponse getCompanyVerificationRequestByCompanyId(Long companyId);
    CompanyVerificationRequestDetailResponse getCompanyVerificationRequestById(Long companyVerificationRequestId);
    CompanyBaseResponse updateCompanyVerificationRequest(Long companyVerificationRequestId, NewCompanyVerificationRequest newCompanyVerificationRequest);
    void changeCompanyStatus(Long companyId, boolean isActive);
    void deleteCompany(Long companyId);
    void reviewCompanyVerificationRequest(Long companyVerificationRequestId, Long reviewedById, boolean isApproved, String reason);
    InvitationResponse createInvitation(Long companyId, Long createdBy, String email, int maxUses, int expiresInHours);
    void markUsed(CompanyInvitation invitation);
    Optional<CompanyInvitation> findByCode(String code);
    Optional<CompanyInvitation> findValidInvitation(String code);
    CompanyInvitation useInvitation(String code);
    List<CompanyInvitation> getInvitationsByCompany(Long companyId);
    List<CompanyInvitation> getActiveInvitationsByCompany(Long companyId);
    void deactivateInvitation(Long invitationId);
    void deleteInvitation(Long invitationId);
    boolean isInvitationBelongsToCompany(Long invitationId, Long companyId);
    int cleanupExpiredInvitations();
    int deactivateExpiredInvitations();
    boolean isEmailMatchInvitation(String code, String email);
    CompanyInvitation extendInvitation(Long invitationId, Integer additionalHours);
    CompanyInvitation resetUsageCount(Long invitationId);
}
