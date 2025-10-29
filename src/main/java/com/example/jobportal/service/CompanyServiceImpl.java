package com.example.jobportal.service;

import com.example.jobportal.dto.request.NewCompanyVerificationRequest;
import com.example.jobportal.dto.request.UpdateCompanyRequest;
import com.example.jobportal.dto.response.CompanyBaseResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestDetailResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestResponse;
import com.example.jobportal.exception.CompanyException;
import com.example.jobportal.model.entity.BaseAddress;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.CompanyVerificationRequest;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.CompanyVerificationRequestRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyVerificationRequestRepository companyVerificationRequestRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CompanyVerificationRequestResponse> getAllCompanyVerificationRequest(String keyword, String verifyStatus, Date createdDate, Pageable pageable) {
        return  companyVerificationRequestRepository.getAllCompanyVerificationRequest(keyword, CompanyVerificationStatus.valueOf(verifyStatus),createdDate,pageable);
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


        if (companyRequest.getName() != null) {
            company.setName(companyRequest.getName());
        }
        if (companyRequest.getEmail() != null) {
            company.setEmail(companyRequest.getEmail());
        }
        if (companyRequest.getStreet() != null & companyRequest.getCity() != null & companyRequest.getWard() != null & companyRequest.getCountry() != null & companyRequest.getDistrict() != null) {
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
        log.info("Successfully updated company with id: {}", companyId);

        return CompanyBaseResponse.fromEntity(updatedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyBaseResponse getCompanyById(Long companyId) {
        log.debug("Getting company by id: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyException.notFound("Company not found with id: " + companyId));

        return CompanyBaseResponse.fromEntity(company);
    }

    @Override
    @Transactional
    public CompanyBaseResponse createCompanyVerificationRequest(NewCompanyVerificationRequest request) {
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
                .name(request.getName())
                .email(request.getEmail())
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

        return CompanyBaseResponse.fromEntity(savedRequest.getCompany());
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

        // Only allow updates for PENDING or REJECTED requests
        if (verificationRequest.getStatus() == CompanyVerificationStatus.APPROVED) {
            throw CompanyException.badRequest("Cannot update an approved verification request");
        }

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> CompanyException.notFound("User not found with id: " + request.getSenderId()));

        BaseAddress address = createBaseAddress(request);

        // Update fields
        verificationRequest.setName(request.getName());
        verificationRequest.setEmail(request.getEmail());
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
        log.info("Reviewing company verification request with id: {} by reviewer id: {}, approved: {}",
                requestId, reviewedById, isApproved);

        CompanyVerificationRequest verificationRequest = companyVerificationRequestRepository.findById(requestId)
                .orElseThrow(() -> CompanyException.notFound("Company verification request not found with id: " + requestId));

        // Check if request is in pending status
        if (verificationRequest.getStatus() != CompanyVerificationStatus.PENDING) {
            throw CompanyException.badRequest("Only pending requests can be reviewed. Current status: " + verificationRequest.getStatus());
        }

        User reviewer = userRepository.findById(reviewedById)
                .orElseThrow(() -> CompanyException.notFound("Reviewer not found with id: " + reviewedById));

        if (isApproved) {
            // Create new company
            Company newCompany = Company.builder()
                    .name(verificationRequest.getName())
                    .email(verificationRequest.getEmail())
                    .address(verificationRequest.getAddress())
                    .isVerified(true)
                    .isActive(true)
                    .build();

            Company savedCompany = companyRepository.save(newCompany);
            log.info("Created new company with id: {}", savedCompany.getId());

            // Update verification request
            verificationRequest.setCompany(savedCompany);
            verificationRequest.setStatus(CompanyVerificationStatus.APPROVED);
        } else {
            // Reject request
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
        log.info("Successfully {} company verification request with id: {}",
                isApproved ? "approved" : "rejected", requestId);
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
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw CompanyException.badRequest("Company name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw CompanyException.badRequest("Company email is required");
        }
        if (request.getTaxCode() == null || request.getTaxCode().trim().isEmpty()) {
            throw CompanyException.badRequest("Tax code is required");
        }
        if (request.getSenderId() == null) {
            throw CompanyException.badRequest("Sender ID is required");
        }
    }
}