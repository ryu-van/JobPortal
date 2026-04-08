package com.example.jobportal.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.jobportal.dto.request.AddressRequest;
import com.example.jobportal.dto.response.*;
import com.example.jobportal.model.entity.*;
import com.example.jobportal.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.jobportal.constant.AppConstants;
import com.example.jobportal.dto.request.NewCompanyVerificationRequest;
import com.example.jobportal.dto.request.UpdateCompanyRequest;
import com.example.jobportal.exception.CompanyException;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import com.example.jobportal.model.enums.NotificationType;
import com.example.jobportal.model.enums.UploadType;
import com.example.jobportal.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyVerificationRequestRepository companyVerificationRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CompanyInvitationRepository invitationRepository;
    private final IndustryRepository industryRepository;
    private static final String INVITATION_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITATION_CODE_LENGTH = 8;
    private final AddressHelper addressHelper;
    private final FileUploadService fileUploadService;
    private final JobRepository jobRepository;

    @Override
    public Page<CompanyVerificationRequestResponse> getAllCompanyVerificationRequest(String keyword, String verifyStatus, LocalDate createdDate, Pageable pageable) {
        CompanyVerificationStatus status = null;
        if (verifyStatus != null && !verifyStatus.equalsIgnoreCase("ALL")) {
            try {
                status = CompanyVerificationStatus.valueOf(verifyStatus);
            } catch (IllegalArgumentException e) {
                throw CompanyException.badRequest("Invalid verification status: " + verifyStatus);
            }
        }
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.toLowerCase() + "%";
        LocalDateTime createdAtStart = createdDate != null ? createdDate.atStartOfDay() : null;
        return companyVerificationRequestRepository.getAllCompanyVerificationRequest(keywordPattern, status, createdAtStart, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyBaseResponse> getAllCompanies(String keyword, String location, Boolean isActive, Pageable pageable) {
        log.debug("Getting all companies with keyword: {}, location: {}, isActive: {}", keyword, location, isActive);
        return companyRepository.getAllCompanies(keyword, location, isActive, pageable);
    }

    @Override
    @Transactional
    public CompanyBaseResponse updateCompany(Long companyId, UpdateCompanyRequest request) {
        log.info("Updating company with id: {}", companyId);

        Company company = companyRepository.findById(companyId).orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));

        if (request.getName() != null) company.setName(request.getName());
        if (request.getEmail() != null) company.setEmail(request.getEmail());
        if (request.getDescription() != null) company.setDescription(request.getDescription());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getLogoUrl() != null) company.setLogoUrl(request.getLogoUrl());
        if (request.getIndustryId() != null) {
            company.setIndustry(industryRepository.findById(request.getIndustryId()).orElseThrow(() -> CompanyException.notFound("Industry not found")));
        }
        if (request.getCompanySize() != null) company.setCompanySize(request.getCompanySize());
        if (request.getEstablishmentDate() != null){
            company.setEstablishmentDate(request.getEstablishmentDate().atStartOfDay());
        }
        if (request.getTaxCode() != null) company.setTaxCode(request.getTaxCode());
        if (request.getAddressRequest() != null) {
            long primaryCount = request.getAddressRequest().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                    .count();

            if (primaryCount > 1) {
                throw CompanyException.badRequest("Chỉ được có một địa chỉ chính");
            }

            // First unset all existing primary flags to avoid the partial unique index
            // violation that occurs when DELETE and INSERT happen in the same flush
            company.getAddresses().forEach(a -> a.setIsPrimary(false));

            // Remove addresses that are no longer in the request
            company.getAddresses().clear();

            // Flush the deletes before inserting new rows so the partial unique index
            // (company_id) WHERE is_primary = true never sees two primary rows at once
            companyRepository.saveAndFlush(company);

            for (AddressRequest addressRequest : request.getAddressRequest()) {
                Address newAddress = addressHelper.build(addressRequest);
                newAddress.setCompany(company);
                company.getAddresses().add(newAddress);
            }
        }

        return CompanyBaseResponse.fromEntity(company);
    }


    @Override
    @Transactional(readOnly = true)
    public CompanyDetailResponse getDeatailCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));
        return CompanyDetailResponse.fromEntity(company);
    }

    @Override
    @Transactional
    public CompanyVerificationRequestResponse createCompanyVerificationRequest(NewCompanyVerificationRequest request, List<MultipartFile> documents, MultipartFile logo) {

        validateCompanyVerificationRequest(request);

        Long senderId = SecurityUtils.currentUserId();

        log.info("Creating company verification request for user id: {}", senderId);

        User sender = userRepository.findById(senderId).orElseThrow(() -> CompanyException.notFound("User not found with id: " + senderId));

        if (documents == null || documents.size() < 2) {
            throw new IllegalArgumentException("At least two documents are required: Business license and tax certificate.");
        }
        if (documents.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 documents.");
        }

        boolean isAdmin = sender.getRole().getName().equalsIgnoreCase("ADMIN");

        if (!isAdmin && companyVerificationRequestRepository.existsByUserAndStatus(sender, CompanyVerificationStatus.PENDING)) {
            throw CompanyException.badRequest("User already has a pending verification request");
        }

        List<Address> addresses = new ArrayList<>();

        if (request.getAddressRequest() != null) {
            for (AddressRequest ar : request.getAddressRequest()) {
                Address addr = addressHelper.build(ar);
                addresses.add(addr);
            }
        }
        Industry industry = null;
        if (request.getIndustryId() != null) {
            industry = industryRepository.findById(request.getIndustryId()).orElse(null);
        }

        CompanyVerificationRequest verificationRequest = CompanyVerificationRequest.builder()
                .companyName(request.getCompanyName())
                .taxCode(request.getTaxCode())
                .contactPerson(request.getContactPerson())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .website(request.getWebsite())
                .industry(industry)
                .description(request.getDescription())
                .companySize(request.getCompanySize())
                .establishmentDate(request.getEstablishmentDate())
                .user(sender)
                .status(isAdmin ? CompanyVerificationStatus.APPROVED : CompanyVerificationStatus.PENDING)
                .build();

        if (addresses != null && !addresses.isEmpty()) {
            verificationRequest.setAddresses(new java.util.HashSet<>(addresses));
        }

        if (isAdmin) {
            verificationRequest.setReviewedBy(sender);
            verificationRequest.setReviewedAt(LocalDateTime.now());
        }

        if (logo != null && !logo.isEmpty()) {
            UploadResultResponse logoResult = fileUploadService.uploadSingle(logo, UploadType.IMAGES);
            if ("SUCCESS".equals(logoResult.getStatus())) {
                verificationRequest.setLogoUrl(logoResult.getUrl());
                verificationRequest.setLogoPublicId(logoResult.getPublicId());
            }
        }

        List<UploadResultResponse> uploadResults = fileUploadService.uploadFiles(documents, UploadType.DOCUMENTS);
        List<CompanyVerificationDocument> documentFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (UploadResultResponse result : uploadResults) {
            if ("SUCCESS".equals(result.getStatus())) {
                documentFiles.add(CompanyVerificationDocument.builder()
                        .fileUrl(result.getUrl())
                        .publicId(result.getPublicId())
                        .fileType(result.getOriginalFilename())
                        .verificationRequest(verificationRequest)
                        .build());
            } else {
                failedFiles.add(result.getOriginalFilename());
            }
        }


        if (documentFiles.size() < 2) {
            documentFiles.forEach(doc -> {
                try {
                    fileUploadService.deleteFile(doc.getPublicId());
                } catch (Exception e) {
                    log.error("Failed to cleanup file {}", doc.getPublicId(), e);
                }
            });

            throw new RuntimeException(String.format("Failed to upload required documents. Success: %d/%d. Failed files: %s", documentFiles.size(), documents.size(), String.join(", ", failedFiles)));
        }
        verificationRequest.setDocuments(documentFiles);
        CompanyVerificationRequest savedRequest = companyVerificationRequestRepository.save(verificationRequest);
        log.info("Successfully created company verification request with id: {}", savedRequest.getId());

        if (isAdmin) {
            Company newCompany = Company.builder()
                    .name(savedRequest.getCompanyName())
                    .email(savedRequest.getContactEmail())
                    .phoneNumber(savedRequest.getContactPhone())
                    .logoUrl(savedRequest.getLogoUrl())
                    .logoPublicId(savedRequest.getLogoPublicId())
                    .taxCode(savedRequest.getTaxCode())
                    .industry(savedRequest.getIndustry())
                    .website(savedRequest.getWebsite())
                    .companySize(savedRequest.getCompanySize())
                    .description(savedRequest.getDescription())
                    .establishmentDate(savedRequest.getEstablishmentDate())
                    .isVerified(true)
                    .isActive(true)
                    .build();

            Company savedCompany = companyRepository.save(newCompany);
            log.info("Created new company with id: {} for admin request", savedCompany.getId());

            if (addresses != null) {
                for (Address src : addresses) {
                    // Copy data into a new Address row — never reuse the same entity
                    // that was added to the verificationRequest's addresses collection.
                    Address copy = new Address();
                    copy.setAddressType(src.getAddressType());
                    copy.setProvinceCode(src.getProvinceCode());
                    copy.setProvinceName(src.getProvinceName());
                    copy.setCommuneCode(src.getCommuneCode());
                    copy.setCommuneName(src.getCommuneName());
                    copy.setDetailAddress(src.getDetailAddress());
                    copy.setIsPrimary(src.getIsPrimary());
                    copy.setIsActive(src.getIsActive());
                    copy.setCompany(savedCompany);
                    savedCompany.getAddresses().add(copy);
                }
            }

            savedRequest.setCompany(savedCompany);
            companyVerificationRequestRepository.save(savedRequest);
        } else {
            try {
                notificationService.createNotificationForRole(AppConstants.ROLE_ADMIN, "Yêu cầu xác minh công ty mới", sender.getFullName() + " vừa gửi yêu cầu xác minh công ty: " + savedRequest.getCompanyName(), NotificationType.COMPANY_VERIFY_REQUESTED, savedRequest.getId(), "COMPANY_VERIFICATION_REQUEST");
                log.debug("Notification sent to admins for verification request id: {}", savedRequest.getId());
            } catch (Exception e) {
                log.error("Failed to send notification for verification request id: {}", savedRequest.getId(), e);
            }
        }

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

        CompanyVerificationRequest request = companyVerificationRequestRepository.findById(requestId).orElseThrow(() -> CompanyException.notFound("Company verification request not found with id: " + requestId));

        return CompanyVerificationRequestDetailResponse.fromEntity(request);
    }

    @Override
    @Transactional
    public CompanyVerificationRequestResponse updateCompanyVerificationRequest(Long requestId, NewCompanyVerificationRequest request, List<MultipartFile> newDocuments, MultipartFile newLogo) {
        log.info("Updating company verification request with id: {}", requestId);

        validateCompanyVerificationRequest(request);

        CompanyVerificationRequest verificationRequest = companyVerificationRequestRepository.findById(requestId).orElseThrow(() -> CompanyException.notFound("Company verification request not found with id: " + requestId));

        Long currentUserId = SecurityUtils.currentUserId();

        if (!verificationRequest.getUser().getId().equals(currentUserId)) {
            throw CompanyException.forbidden("You don't have permission to update this request");
        }

        if (verificationRequest.getStatus() == CompanyVerificationStatus.APPROVED) {
            throw CompanyException.badRequest("Cannot update an approved verification request");
        }

        verificationRequest.setCompanyName(request.getCompanyName());
        verificationRequest.setTaxCode(request.getTaxCode());
        verificationRequest.setContactPerson(request.getContactPerson());
        verificationRequest.setContactEmail(request.getContactEmail());
        verificationRequest.setContactPhone(request.getContactPhone());
        if (request.getAddressRequest() != null) {
            long primaryCount = request.getAddressRequest().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                    .count();

            if (primaryCount > 1) {
                throw CompanyException.badRequest("Chỉ được có một địa chỉ chính");
            }

            verificationRequest.getAddresses().clear();
            for (AddressRequest ar : request.getAddressRequest()) {
                Address addr = addressHelper.build(ar);
                verificationRequest.getAddresses().add(addr);
            }
        }

        if (newLogo != null && !newLogo.isEmpty()) {
            if (verificationRequest.getLogoPublicId() != null) {
                fileUploadService.deleteFile(verificationRequest.getLogoPublicId());
            }
            UploadResultResponse logoResult = fileUploadService.uploadSingle(newLogo, UploadType.IMAGES);
            if ("SUCCESS".equals(logoResult.getStatus())) {
                verificationRequest.setLogoUrl(logoResult.getUrl());
                verificationRequest.setLogoPublicId(logoResult.getPublicId());
            }
        }

        if (verificationRequest.getStatus() == CompanyVerificationStatus.REJECTED) {
            verificationRequest.setStatus(CompanyVerificationStatus.PENDING);
            verificationRequest.setRejectionReason(null);
            verificationRequest.setReviewedAt(null);
            verificationRequest.setReviewedBy(null);
            log.info("Reset status to PENDING for rejected request id: {}", requestId);
        }
        if (newDocuments != null && !newDocuments.isEmpty()) {
            updateDocuments(verificationRequest, newDocuments);
        }
        verificationRequest.setStatus(CompanyVerificationStatus.PENDING);

        CompanyVerificationRequest updatedRequest = companyVerificationRequestRepository.save(verificationRequest);
        log.info("Successfully updated company verification request with id: {}", requestId);
        if (verificationRequest.getStatus() == CompanyVerificationStatus.PENDING) {
            try {
                notificationService.createNotificationForRole(
                        "ADMIN",
                        "Yêu cầu xác minh công ty đã cập nhật",
                        verificationRequest.getUser().getFullName() +
                                " vừa cập nhật yêu cầu xác minh công ty: " + updatedRequest.getCompanyName(),
                        NotificationType.COMPANY_VERIFY_REQUESTED,
                        updatedRequest.getId(),
                        "COMPANY_VERIFICATION_REQUEST"
                );
            } catch (Exception e) {
                log.error("Failed to send notification for updated request id: {}", requestId, e);
            }
        }
        return CompanyVerificationRequestResponse.fromEntity(updatedRequest);
    }

    @Override
    @Transactional
    public void changeCompanyStatus(Long companyId, boolean isActive) {
        log.info("Changing company status for id: {} to isActive: {}", companyId, isActive);

        Company company = companyRepository.findById(companyId).orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));

        company.setIsActive(isActive);
        companyRepository.save(company);

        log.info("Successfully changed company status for id: {}", companyId);
    }

    @Override
    @Transactional
    public void deleteCompany(Long companyId) {
        log.info("Deleting company with id: {}", companyId);

        Company company = companyRepository.findById(companyId).orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));
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
                    .phoneNumber(verificationRequest.getContactPhone())
                    .taxCode(verificationRequest.getTaxCode())
                    .website(verificationRequest.getWebsite())
                    .logoUrl(verificationRequest.getLogoUrl())
                    .logoPublicId(verificationRequest.getLogoPublicId())
                    .industry(verificationRequest.getIndustry())
                    .isVerified(true)
                    .isActive(true)
                    .build();

            Company savedCompany = companyRepository.save(newCompany);
            log.info("Created new company with id: {}", savedCompany.getId());

            // Copy address DATA into brand-new Address rows owned by the Company.
            // Never reuse/reassign existing Address entities — they may be referenced
            // by other entities (e.g. CompanyVerificationRequest) and sharing them
            // causes FK violations when either owner deletes its addresses.
            if (verificationRequest.getAddresses() != null) {
                for (Address src : verificationRequest.getAddresses()) {
                    Address copy = new Address();
                    copy.setAddressType(src.getAddressType());
                    copy.setProvinceCode(src.getProvinceCode());
                    copy.setProvinceName(src.getProvinceName());
                    copy.setCommuneCode(src.getCommuneCode());
                    copy.setCommuneName(src.getCommuneName());
                    copy.setDetailAddress(src.getDetailAddress());
                    copy.setIsPrimary(src.getIsPrimary());
                    copy.setIsActive(src.getIsActive());
                    copy.setCompany(savedCompany);
                    savedCompany.getAddresses().add(copy);
                }
                companyRepository.save(savedCompany);
            }

            verificationRequest.setCompany(savedCompany);
            verificationRequest.setStatus(CompanyVerificationStatus.APPROVED);

        } else {
            if (reason == null || reason.trim().isEmpty()) {
                throw CompanyException.badRequest("Rejection reason is required");
            }
            verificationRequest.setStatus(CompanyVerificationStatus.REJECTED);
            verificationRequest.setRejectionReason(reason);
        }

        verificationRequest.setReviewedBy(reviewer);
        verificationRequest.setReviewedAt(LocalDateTime.now());

        companyVerificationRequestRepository.save(verificationRequest);

        User requester = verificationRequest.getUser();
        if (isApproved) {
            notificationService.createNotification(
                    requester.getId(),
                    "Yêu cầu xác minh công ty được duyệt",
                    "Công ty '" + verificationRequest.getCompanyName() + "' đã được xác minh thành công.",
                    NotificationType.COMPANY_VERIFIED,
                    verificationRequest.getId(),
                    "COMPANY_VERIFICATION_REQUEST"
            );
        } else {
            notificationService.createNotification(
                    requester.getId(),
                    "Yêu cầu xác minh công ty bị từ chối",
                    "Yêu cầu xác minh công ty '" + verificationRequest.getCompanyName() + "' đã bị từ chối. Lý do: " + reason,
                    NotificationType.COMPANY_REJECTED,
                    verificationRequest.getId(),
                    "COMPANY_VERIFICATION_REQUEST"
            );
        }
    }

    @Override
    @Transactional
    public InvitationResponse createInvitation(Long companyId, Long createdBy, String email, int maxUses, int expiresInHours) {
        log.info("Creating invitation for company: {} by user: {}", companyId, createdBy);
        Company company = companyRepository.findById(companyId).orElseThrow(() -> CompanyException.notFound("Company not found"));
        User user = userRepository.findById(createdBy).orElseThrow(() -> CompanyException.notFound("User not found"));

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
        return invitationRepository.findByCode(code).filter(invitation -> {
            boolean isValid = invitation.canBeUsed();
            log.debug("Invitation {} valid status: {}", code, isValid);
            return isValid;
        });
    }

    @Override
    @Transactional
    public CompanyInvitation useInvitation(String code) {
        log.info("Using invitation: {}", code);

        CompanyInvitation invitation = findValidInvitation(code).orElseThrow(() -> CompanyException.badRequest("Mã mời không hợp lệ, đã hết hạn hoặc đã được sử dụng hết"));

        invitation.setUsedCount(invitation.getUsedCount() + 1);

        if (invitation.getUsedCount() >= invitation.getMaxUses()) {
            invitation.setIsActive(false);
            log.info("🔒 Invitation {} has been fully used and deactivated", code);
        }

        CompanyInvitation saved = invitationRepository.save(invitation);
        log.info("✅ Invitation {} used successfully ({}/{})", code, saved.getUsedCount(), saved.getMaxUses());

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

        CompanyInvitation invitation = invitationRepository.findById(invitationId).orElseThrow(() -> CompanyException.notFound("Invitation not found with id: " + invitationId));

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

        return invitationRepository.findById(invitationId).map(invitation -> invitation.getCompany().getId().equals(companyId)).orElse(false);
    }

    @Override
    @Transactional
    public int cleanupExpiredInvitations() {
        log.info("Starting cleanup of expired invitations");

        List<CompanyInvitation> expiredInvitations = invitationRepository.findExpiredInvitations(LocalDateTime.now());

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

        List<CompanyInvitation> expiredInvitations = invitationRepository.findExpiredActiveInvitations(LocalDateTime.now());

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

        return invitationRepository.findByCode(code).map(invitation -> {
            if (invitation.getEmail() == null || invitation.getEmail().trim().isEmpty()) {
                return true;
            }
            return invitation.getEmail().equalsIgnoreCase(email);
        }).orElse(false);
    }

    @Override
    @Transactional
    public CompanyInvitation extendInvitation(Long invitationId, Integer additionalHours) {
        log.info("Extending invitation {} by {} hours", invitationId, additionalHours);

        if (additionalHours == null || additionalHours <= 0) {
            throw CompanyException.badRequest("Additional hours must be greater than 0");
        }

        CompanyInvitation invitation = invitationRepository.findById(invitationId).orElseThrow(() -> CompanyException.notFound("Invitation not found with id: " + invitationId));

        LocalDateTime oldExpiry = invitation.getExpiresAt();
        invitation.setExpiresAt(oldExpiry.plusHours(additionalHours));

        CompanyInvitation saved = invitationRepository.save(invitation);
        log.info("✅ Extended invitation {} from {} to {}", invitationId, oldExpiry, saved.getExpiresAt());

        return saved;
    }


    @Override
    @Transactional
    public CompanyInvitation resetUsageCount(Long invitationId) {
        log.info("Resetting usage count for invitation: {}", invitationId);

        CompanyInvitation invitation = invitationRepository.findById(invitationId).orElseThrow(() -> CompanyException.notFound("Invitation not found with id: " + invitationId));

        int oldCount = invitation.getUsedCount();
        invitation.setUsedCount(0);
        invitation.setIsActive(true);

        CompanyInvitation saved = invitationRepository.save(invitation);
        log.info("✅ Reset invitation {} usage count from {} to 0", invitationId, oldCount);

        return saved;
    }

    @Override
    public List<CompanyBaseResponse> getListOfCompanies(String keyword) {
        return companyRepository.getListCompany(keyword);
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
            code.append(INVITATION_CODE_CHARACTERS.charAt(random.nextInt(INVITATION_CODE_CHARACTERS.length())));
        }

        return code.toString();
    }
    private void updateDocuments(
            CompanyVerificationRequest verificationRequest,
            List<MultipartFile> newDocuments) {

        int currentDocCount = verificationRequest.getDocuments().size();
        int newDocCount = newDocuments.size();

        if (currentDocCount + newDocCount > 10) {
            throw new IllegalArgumentException(
                    String.format("Maximum 10 documents. Current: %d, trying to add: %d",
                            currentDocCount, newDocCount)
            );
        }

        List<String> failedFiles = new ArrayList<>();
        int successCount = 0;

        for (MultipartFile file : newDocuments) {
            try {
                UploadResultResponse result = fileUploadService.uploadSingle(
                        file,
                        UploadType.DOCUMENTS
                );

                if ("SUCCESS".equals(result.getStatus())) {
                    CompanyVerificationDocument docFile =
                            CompanyVerificationDocument.builder()
                                    .fileUrl(result.getUrl())
                                    .publicId(result.getPublicId())
                                    .fileType(file.getOriginalFilename())
                                    .verificationRequest(verificationRequest)
                                    .build();

                    verificationRequest.addDocument(docFile);
                    successCount++;
                    log.debug("Successfully uploaded document: {}", file.getOriginalFilename());
                } else {
                    failedFiles.add(file.getOriginalFilename());
                    log.error("Failed to upload file: {}", file.getOriginalFilename());
                }
            } catch (Exception e) {
                failedFiles.add(file.getOriginalFilename());
                log.error("Error uploading file: {}", file.getOriginalFilename(), e);
            }
        }

        if (successCount == 0 && !failedFiles.isEmpty()) {
            throw new RuntimeException("All documents failed to upload: " + String.join(", ", failedFiles));
        }

        if (!failedFiles.isEmpty()) {
            log.warn("Some documents failed to upload: {}", String.join(", ", failedFiles));
        }
    }


}
