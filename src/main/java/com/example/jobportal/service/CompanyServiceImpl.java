package com.example.jobportal.service;

import com.example.jobportal.dto.request.NewCompanyVerificationRequest;
import com.example.jobportal.dto.request.UpdateCompanyRequest;
import com.example.jobportal.dto.response.CompanyBaseResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestDetailResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestResponse;
import com.example.jobportal.dto.response.InvitationResponse;
import com.example.jobportal.exception.CompanyException;
import com.example.jobportal.model.entity.*;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import com.example.jobportal.model.enums.NotificationType;
import com.example.jobportal.repository.CompanyInvitationRepository;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.CompanyVerificationRequestRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyVerificationRequestRepository companyVerificationRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CompanyInvitationRepository invitationRepository;
    private static final String INVITATION_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITATION_CODE_LENGTH = 8;

    @Override
    public Page<CompanyVerificationRequestResponse> getAllCompanyVerificationRequest(String keyword, String verifyStatus, LocalDate createdDate, Pageable pageable) {
        CompanyVerificationStatus status = null;
        if (verifyStatus != null && !verifyStatus.equalsIgnoreCase("ALL")) {
            try {
                status = CompanyVerificationStatus.valueOf(verifyStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw CompanyException.badRequest("Invalid verification status: " + verifyStatus);
            }
        }
        return companyVerificationRequestRepository.getAllCompanyVerificationRequest(keyword, status, createdDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyBaseResponse> getAllCompanies(String keyword, String location, Boolean isActive, Pageable pageable) {
        log.debug("Getting all companies with keyword: {}, location: {}, isActive: {}", keyword, location, isActive);
        return companyRepository.getAllCompanies(keyword, location, isActive, pageable);
    }

    @Override
    @Transactional
    public CompanyBaseResponse updateCompany(Long companyId, UpdateCompanyRequest companyRequest) {
        log.info("Updating company with id: {}", companyId);

        if (companyRequest == null) {
            throw CompanyException.badRequest("Company update request cannot be null");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));


        if (companyRequest.getName() != null) company.setName(companyRequest.getName());
        if (companyRequest.getEmail() != null) company.setEmail(companyRequest.getEmail());
        if (companyRequest.getStreet() != null && companyRequest.getCity() != null
                && companyRequest.getWard() != null && companyRequest.getCountry() != null
                && companyRequest.getDistrict() != null) {
            BaseAddress baseAddress = new BaseAddress();
            baseAddress.setStreet(companyRequest.getStreet());
            baseAddress.setCity(companyRequest.getCity());
            baseAddress.setWard(companyRequest.getWard());
            baseAddress.setCountry(companyRequest.getCountry());
            baseAddress.setDistrict(companyRequest.getDistrict());
            company.setAddress(baseAddress);
        }
        if (companyRequest.getDescription() != null) {
            company.setDescription(companyRequest.getDescription());
        }
        if (companyRequest.getWebsite() != null) {
            company.setWebsite(companyRequest.getWebsite());
        }
        if (companyRequest.getLogoUrl() != null) {
            company.setLogoUrl(companyRequest.getLogoUrl());
        }

        Company updatedCompany = companyRepository.save(company);
        return CompanyBaseResponse.fromEntity(updatedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyBaseResponse getCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));
        return CompanyBaseResponse.fromEntity(company);
    }

    @Override
    @Transactional
    public CompanyVerificationRequestResponse createCompanyVerificationRequest(NewCompanyVerificationRequest request) {
        log.info("Creating company verification request for user id: {}", request.getSenderId());

        validateCompanyVerificationRequest(request);

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> CompanyException.notFound("User not found with id: " + request.getSenderId()));

        // Check if user already has a pending verification request
        if (companyVerificationRequestRepository.existsByUserAndStatus(sender, CompanyVerificationStatus.PENDING)) {
            throw CompanyException.badRequest("User already has a pending verification request");
        }

        BaseAddress address = createBaseAddress(request);

        CompanyVerificationRequest verificationRequest = CompanyVerificationRequest.builder()
                .companyName(request.getCompanyName())
                .businessLicense(request.getBusinessLicense())
                .taxCode(request.getTaxCode())
                .contactPerson(request.getContactPerson())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(address)
                .documents(request.getDocuments())
                .requestedRole(request.getRequestedRole())
                .user(sender)
                .status(CompanyVerificationStatus.PENDING)
                .build();

        CompanyVerificationRequest savedRequest = companyVerificationRequestRepository.save(verificationRequest);
        log.info("Successfully created company verification request with id: {}", savedRequest.getId());
        notificationService.createNotificationForRole(
                "ADMIN",
                "Yêu cầu xác minh công ty mới",
                sender.getFullName() + " vừa gửi yêu cầu xác minh công ty: " + savedRequest.getCompanyName(),
                NotificationType.COMPANY_VERIFY_REQUESTED.name(),
                savedRequest.getId(),
                "COMPANY_VERIFICATION_REQUEST"
        );

        return CompanyVerificationRequestResponse.fromEntity(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyVerificationRequestDetailResponse getCompanyVerificationRequestByCompanyId(Long companyId) {
        log.debug("Getting company verification request by company id: {}", companyId);

        CompanyVerificationRequest request = companyVerificationRequestRepository.findByCompanyId(companyId);

        if (request == null) {
            throw CompanyException.notFound("Company verification request not found for company id: " + companyId);
        }

        return CompanyVerificationRequestDetailResponse.fromEntity(request);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyVerificationRequestDetailResponse getCompanyVerificationRequestById(Long requestId) {
        log.debug("Getting company verification request by id: {}", requestId);

        CompanyVerificationRequest request = companyVerificationRequestRepository.findById(requestId)
                .orElseThrow(() -> CompanyException.notFound("Company verification request not found with id: " + requestId));

        return CompanyVerificationRequestDetailResponse.fromEntity(request);
    }

    @Override
    @Transactional
    public CompanyBaseResponse updateCompanyVerificationRequest(Long requestId, NewCompanyVerificationRequest request) {
        log.info("Updating company verification request with id: {}", requestId);

        validateCompanyVerificationRequest(request);

        CompanyVerificationRequest verificationRequest = companyVerificationRequestRepository.findById(requestId)
                .orElseThrow(() -> CompanyException.notFound("Company verification request not found with id: " + requestId));

        if (verificationRequest.getStatus() == CompanyVerificationStatus.APPROVED) {
            throw CompanyException.badRequest("Cannot update an approved verification request");
        }

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> CompanyException.notFound("User not found with id: " + request.getSenderId()));

        BaseAddress address = createBaseAddress(request);

        verificationRequest.setCompanyName(request.getCompanyName());
        verificationRequest.setBusinessLicense(request.getBusinessLicense());
        verificationRequest.setTaxCode(request.getTaxCode());
        verificationRequest.setContactPerson(request.getContactPerson());
        verificationRequest.setContactEmail(request.getContactEmail());
        verificationRequest.setContactPhone(request.getContactPhone());
        verificationRequest.setAddress(address);
        verificationRequest.setDocuments(request.getDocuments());
        verificationRequest.setRequestedRole(request.getRequestedRole());
        verificationRequest.setUser(sender);
        verificationRequest.setStatus(CompanyVerificationStatus.PENDING);

        CompanyVerificationRequest updatedRequest = companyVerificationRequestRepository.save(verificationRequest);
        log.info("Successfully updated company verification request with id: {}", requestId);

        return CompanyBaseResponse.fromEntity(updatedRequest.getCompany());
    }

    @Override
    @Transactional
    public void changeCompanyStatus(Long companyId, boolean isActive) {
        log.info("Changing company status for id: {} to isActive: {}", companyId, isActive);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));

        company.setIsActive(isActive);
        companyRepository.save(company);

        log.info("Successfully changed company status for id: {}", companyId);
    }

    @Override
    @Transactional
    public void deleteCompany(Long companyId) {
        log.info("Deleting company with id: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));

        // Check if company has active job postings or other dependencies
        if (company.getIsActive()) {
            throw CompanyException.badRequest("Cannot delete an active company. Please deactivate it first.");
        }

        companyRepository.delete(company);
        log.info("Successfully deleted company with id: {}", companyId);
    }

    @Override
    @Transactional
    public void reviewCompanyVerificationRequest(Long requestId, Long reviewedById, boolean isApproved, String reason) {
        log.info("Reviewing company verification request id: {}", requestId);

        CompanyVerificationRequest verificationRequest = companyVerificationRequestRepository.findById(requestId)
                .orElseThrow(() -> CompanyException.notFound("Company verification request not found with id: " + requestId));

        if (verificationRequest.getStatus() != CompanyVerificationStatus.PENDING) {
            throw CompanyException.badRequest("Only pending requests can be reviewed");
        }

        User reviewer = userRepository.findById(reviewedById)
                .orElseThrow(() -> CompanyException.notFound("Reviewer not found with id: " + reviewedById));

        if (isApproved) {
            Company newCompany = Company.builder()
                    .name(verificationRequest.getCompanyName())
                    .email(verificationRequest.getContactEmail())
                    .phone(verificationRequest.getContactPhone())
                    .address(verificationRequest.getAddress())
                    .isVerified(true)
                    .isActive(true)
                    .build();

            Company savedCompany = companyRepository.save(newCompany);
            log.info("Created new company with id: {}", savedCompany.getId());

            verificationRequest.setCompany(savedCompany);
            verificationRequest.setStatus(CompanyVerificationStatus.APPROVED);
        } else {
            if (reason == null || reason.trim().isEmpty()) {
                throw CompanyException.badRequest("Rejection reason is required");
            }
            verificationRequest.setStatus(CompanyVerificationStatus.REJECTED);
            verificationRequest.setRejectionReason(reason);
        }

        // Common updates for both approve and reject
        verificationRequest.setReviewedBy(reviewer);
        verificationRequest.setReviewedAt(LocalDateTime.now());

        companyVerificationRequestRepository.save(verificationRequest);

        User requester = verificationRequest.getUser();
        if (isApproved) {
            notificationService.createNotification(
                    requester.getId(),
                    "Yêu cầu xác minh công ty được duyệt",
                    "Công ty '" + verificationRequest.getCompanyName() + "' đã được xác minh thành công.",
                    NotificationType.COMPANY_VERIFIED.name(),
                    verificationRequest.getId(),
                    "COMPANY_VERIFICATION_REQUEST"
            );
        } else {
            notificationService.createNotification(
                    requester.getId(),
                    "Yêu cầu xác minh công ty bị từ chối",
                    "Yêu cầu xác minh công ty '" + verificationRequest.getCompanyName() + "' đã bị từ chối. Lý do: " + reason,
                    NotificationType.COMPANY_REJECTED.name(),
                    verificationRequest.getId(),
                    "COMPANY_VERIFICATION_REQUEST"
            );
        }
    }

    @Override
    @Transactional
    public InvitationResponse createInvitation(Long companyId, Long createdBy, String email, int maxUses, int expiresInHours) {
        log.info("Creating invitation for company: {} by user: {}", companyId, createdBy);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyException.notFound("Company not found"));
        User user = userRepository.findById(createdBy)
                .orElseThrow(()-> CompanyException.notFound("User not found"));

        CompanyInvitation invitation = new CompanyInvitation();
        invitation.setCompany(company);
        invitation.setCreatedBy(user);
        invitation.setCode(generateUniqueInvitationCode());
        invitation.setEmail(email);
        invitation.setMaxUses(maxUses);
        invitation.setUsedCount(0);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(expiresInHours));
        invitation.setIsActive(true);
        invitation.setRole("hr");

        CompanyInvitation saved = invitationRepository.save(invitation);
        log.info("✅ Created invitation with code: {}", saved.getCode());

        return InvitationResponse.fromEntity(saved);
    }


    @Override
    @Transactional
    public void markUsed(CompanyInvitation invitation) {
        log.info("Marking invitation {} as used", invitation.getCode());

        invitation.setUsedCount(invitation.getUsedCount() + 1);

        if (invitation.getUsedCount() >= invitation.getMaxUses()) {
            invitation.setIsActive(false);
            log.info("Invitation {} has reached max uses, deactivated", invitation.getCode());
        }

        invitationRepository.save(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyInvitation> findByCode(String code) {
        log.debug("Finding invitation by code: {}", code);
        return invitationRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyInvitation> findValidInvitation(String code) {
        log.debug("Finding valid invitation by code: {}", code);
        return invitationRepository.findByCode(code)
                .filter(invitation -> {
                    boolean isValid = invitation.canBeUsed();
                    log.debug("Invitation {} valid status: {}", code, isValid);
                    return isValid;
                });
    }

    @Override
    @Transactional
    public CompanyInvitation useInvitation(String code) {
        log.info("Using invitation: {}", code);

        CompanyInvitation invitation = findValidInvitation(code)
                .orElseThrow(() -> CompanyException.badRequest(
                        "Mã mời không hợp lệ, đã hết hạn hoặc đã được sử dụng hết"
                ));

        invitation.setUsedCount(invitation.getUsedCount() + 1);

        if (invitation.getUsedCount() >= invitation.getMaxUses()) {
            invitation.setIsActive(false);
            log.info("🔒 Invitation {} has been fully used and deactivated", code);
        }

        CompanyInvitation saved = invitationRepository.save(invitation);
        log.info("✅ Invitation {} used successfully ({}/{})",
                code, saved.getUsedCount(), saved.getMaxUses());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyInvitation> getInvitationsByCompany(Long companyId) {
        log.debug("Getting all invitations for company: {}", companyId);
        return invitationRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyInvitation> getActiveInvitationsByCompany(Long companyId) {
        log.debug("Getting active invitations for company: {}", companyId);
        return invitationRepository.findByCompanyIdAndIsActiveTrue(companyId);
    }


    @Override
    @Transactional
    public void deactivateInvitation(Long invitationId) {
        log.info("Deactivating invitation: {}", invitationId);

        CompanyInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> CompanyException.notFound("Invitation not found with id: " + invitationId));

        invitation.setIsActive(false);
        invitationRepository.save(invitation);

        log.info("✅ Invitation {} deactivated", invitationId);
    }

    @Override
    @Transactional
    public void deleteInvitation(Long invitationId) {
        log.info("Deleting invitation: {}", invitationId);

        if (!invitationRepository.existsById(invitationId)) {
            throw CompanyException.notFound("Invitation not found with id: " + invitationId);
        }

        invitationRepository.deleteById(invitationId);
        log.info("✅ Invitation {} deleted", invitationId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInvitationBelongsToCompany(Long invitationId, Long companyId) {
        log.debug("Checking if invitation {} belongs to company {}", invitationId, companyId);

        return invitationRepository.findById(invitationId)
                .map(invitation -> invitation.getCompany().getId().equals(companyId))
                .orElse(false);
    }

    @Override
    @Transactional
    public int cleanupExpiredInvitations() {
        log.info("Starting cleanup of expired invitations");

        List<CompanyInvitation> expiredInvitations =
                invitationRepository.findExpiredInvitations(LocalDateTime.now());

        int count = expiredInvitations.size();

        if (count > 0) {
            invitationRepository.deleteAll(expiredInvitations);
            log.info("🗑️ Deleted {} expired invitations", count);
        } else {
            log.info("No expired invitations to cleanup");
        }

        return count;
    }

    @Override
    @Transactional
    public int deactivateExpiredInvitations() {
        log.info("Starting deactivation of expired invitations");

        List<CompanyInvitation> expiredInvitations =
                invitationRepository.findExpiredActiveInvitations(LocalDateTime.now());

        int count = expiredInvitations.size();

        if (count > 0) {
            expiredInvitations.forEach(invitation -> invitation.setIsActive(false));
            invitationRepository.saveAll(expiredInvitations);
            log.info("🔒 Deactivated {} expired invitations", count);
        } else {
            log.info("No expired active invitations to deactivate");
        }

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailMatchInvitation(String code, String email) {
        log.debug("Checking if email {} matches invitation {}", email, code);

        return invitationRepository.findByCode(code)
                .map(invitation -> {
                    // Nếu invitation không chỉ định email cụ thể, cho phép mọi email
                    if (invitation.getEmail() == null || invitation.getEmail().trim().isEmpty()) {
                        return true;
                    }
                    // Nếu có email cụ thể, phải match
                    return invitation.getEmail().equalsIgnoreCase(email);
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public CompanyInvitation extendInvitation(Long invitationId, Integer additionalHours) {
        log.info("Extending invitation {} by {} hours", invitationId, additionalHours);

        if (additionalHours == null || additionalHours <= 0) {
            throw CompanyException.badRequest("Additional hours must be greater than 0");
        }

        CompanyInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> CompanyException.notFound("Invitation not found with id: " + invitationId));

        LocalDateTime oldExpiry = invitation.getExpiresAt();
        invitation.setExpiresAt(oldExpiry.plusHours(additionalHours));

        CompanyInvitation saved = invitationRepository.save(invitation);
        log.info("✅ Extended invitation {} from {} to {}",
                invitationId, oldExpiry, saved.getExpiresAt());

        return saved;
    }


    @Override
    @Transactional
    public CompanyInvitation resetUsageCount(Long invitationId) {
        log.info("Resetting usage count for invitation: {}", invitationId);

        CompanyInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> CompanyException.notFound("Invitation not found with id: " + invitationId));

        int oldCount = invitation.getUsedCount();
        invitation.setUsedCount(0);
        invitation.setIsActive(true);

        CompanyInvitation saved = invitationRepository.save(invitation);
        log.info("✅ Reset invitation {} usage count from {} to 0", invitationId, oldCount);

        return saved;
    }

    // Helper methods
    private BaseAddress createBaseAddress(NewCompanyVerificationRequest request) {
        BaseAddress address = new BaseAddress();
        address.setStreet(request.getStreet());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setCountry(request.getCountry());
        return address;
    }

    private void validateCompanyVerificationRequest(NewCompanyVerificationRequest request) {
        if (request == null) {
            throw CompanyException.badRequest("Company verification request cannot be null");
        }
        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw CompanyException.badRequest("Company name is required");
        }
        if (request.getContactEmail() == null || request.getContactEmail().trim().isEmpty()) {
            throw CompanyException.badRequest("Company email is required");
        }
        if (request.getTaxCode() == null || request.getTaxCode().trim().isEmpty()) {
            throw CompanyException.badRequest("Tax code is required");
        }
        if (request.getSenderId() == null) {
            throw CompanyException.badRequest("Sender ID is required");
        }
    }
    private String generateUniqueInvitationCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = generateRandomInvitationCode();
            attempts++;

            if (attempts >= maxAttempts) {
                throw CompanyException.internal("Failed to generate unique invitation code after " + maxAttempts + " attempts");
            }
        } while (invitationRepository.existsByCode(code));

        return code;
    }


    private String generateRandomInvitationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(INVITATION_CODE_LENGTH);

        for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
            code.append(INVITATION_CODE_CHARACTERS.charAt(
                    random.nextInt(INVITATION_CODE_CHARACTERS.length())
            ));
        }

        return code.toString();
    }
}