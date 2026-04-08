package com.example.jobportal.controller;

import java.time.LocalDate;
import java.util.List;

import com.example.jobportal.dto.request.*;
import com.example.jobportal.dto.response.*;
import com.example.jobportal.service.IndustryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.jobportal.model.enums.UploadType;
import com.example.jobportal.service.CompanyService;
import com.example.jobportal.service.FileUploadService;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/companies")
public class CompanyController extends BaseController {

    private final CompanyService companyService;
    private final FileUploadService fileUploadService;
    private final IndustryService industryService;

    // ─── Company ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyBaseResponse>>> getAllCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CompanyBaseResponse> companyPage =
                companyService.getAllCompanies(keyword, location, isActive, pageable);

        PageInfo pageInfo = PageInfo.of(companyPage.getNumber(), companyPage.getSize(), companyPage.getTotalElements());
        return ok("Get companies successfully", companyPage.getContent(), pageInfo);
    }


    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<CompanyBaseResponse>>> getCompanyDropdown(
            @RequestParam(required = false) String keyword
    ) {
        List<CompanyBaseResponse> responses = companyService.getListOfCompanies(keyword);
        return ok("Get company dropdown list successfully", responses);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompanyDetail(
            @PathVariable Long companyId
    ) {
        CompanyDetailResponse response = companyService.getDeatailCompanyById(companyId);
        return ok("Get company detail successfully", response);
    }

    @PutMapping(value = "/{companyId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CompanyBaseResponse>> updateCompany(
            @PathVariable Long companyId,
            @RequestPart("companyRequest") UpdateCompanyRequest companyRequest,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        if (file != null && !file.isEmpty()) {
            UploadResultResponse uploadResultResponse =
                    fileUploadService.uploadSingle(file, UploadType.IMAGES);
            companyRequest.setLogoUrl(uploadResultResponse.getUrl());
        }
        CompanyBaseResponse response = companyService.updateCompany(companyId, companyRequest);
        return ok("Update company successfully", response);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return ok("Delete company successfully");
    }


    @PatchMapping("/{companyId}/status")
    public ResponseEntity<ApiResponse<Void>> changeCompanyStatus(
            @PathVariable Long companyId,
            @RequestParam Boolean isActive
    ) {
        companyService.changeCompanyStatus(companyId, isActive);
        return ok("Change company status successfully");
    }

    // ─── Invitations ────────────────────────────────────────────────────────────

    @PostMapping("/{companyId}/invitations")
    public ResponseEntity<ApiResponse<InvitationResponse>> createInvitation(
            @PathVariable Long companyId,
            @RequestBody CreateInvitationRequest request
    ) {
        InvitationResponse response = companyService.createInvitation(
                companyId,
                request.getCreatedById(),
                request.getEmail(),
                request.getMaxUses(),
                request.getExpiresInHours()
        );
        return ok("Create invitation successfully", response);
    }

    // ─── Verification Requests ───────────────────────────────────────────────────

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<CompanyVerificationRequestResponse>>> getAllVerificationRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String verifyStatus,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CompanyVerificationRequestResponse> companyPage =
                companyService.getAllCompanyVerificationRequest(keyword, verifyStatus, createdDate, pageable);

        PageInfo pageInfo = PageInfo.of(companyPage.getNumber(), companyPage.getSize(), companyPage.getTotalElements());
        return ok("Get company verification requests successfully", companyPage.getContent(), pageInfo);
    }

    @PostMapping(value = "/requests", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CompanyVerificationRequestResponse>> createCompanyVerificationRequest(
            @RequestPart("data") NewCompanyVerificationRequest request,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    ) {
        CompanyVerificationRequestResponse response =
                companyService.createCompanyVerificationRequest(request, documents, logo);
        return ok("Create company verification request successfully", response);
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<CompanyVerificationRequestDetailResponse>> getVerificationRequestDetail(
            @PathVariable Long requestId
    ) {
        CompanyVerificationRequestDetailResponse response =
                companyService.getCompanyVerificationRequestById(requestId);
        return ok("Get company verification request detail successfully", response);
    }

    @GetMapping("/{companyId}/requests")
    public ResponseEntity<ApiResponse<CompanyVerificationRequestDetailResponse>> getVerificationRequestByCompanyId(
            @PathVariable Long companyId
    ) {
        CompanyVerificationRequestDetailResponse response =
                companyService.getCompanyVerificationRequestByCompanyId(companyId);
        return ok("Get company verification request by company id successfully", response);
    }

    @PutMapping(value = "/requests/{requestId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CompanyVerificationRequestResponse>> updateCompanyVerificationRequest(
            @PathVariable Long requestId,
            @RequestPart("data") NewCompanyVerificationRequest request,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    ) {
        CompanyVerificationRequestResponse response =
                companyService.updateCompanyVerificationRequest(requestId, request, documents, logo);
        return ok("Update company verification request successfully", response);
    }

    @PutMapping("/requests/{requestId}/review")
    public ResponseEntity<ApiResponse<Void>> reviewCompanyVerificationRequest(
            @PathVariable Long requestId,
            @RequestBody ReviewCompanyVerificationRequest request
    ) {
        companyService.reviewCompanyVerificationRequest(
                requestId,
                request.getReviewedById(),
                request.isApproved(),
                request.getReason()
        );
        return ok("Review company verification request successfully");
    }
    // ─── Industries ───────────────────────────────────────────────────
    @GetMapping("/industries")
    public ResponseEntity<ApiResponse<List<IndustryResponse>>> getAllIndustries(
            @RequestParam(required = false) String name
    ) {
        List<IndustryResponse> industryResponseList = industryService.getAllIndustry(name);
        return ok("Get industries successfully", industryResponseList);
    }

    @GetMapping("/industries/{id}")
    public ResponseEntity<ApiResponse<IndustryResponse>> getIndustryDetail(
            @PathVariable Long id
    ) {
        IndustryResponse response = industryService.getIndustryById(id);
        return ok("Get industry successfully", response);
    }

    @PostMapping("/industries")
    public ResponseEntity<ApiResponse<IndustryResponse>> createIndustry(
            @Valid @RequestBody IndustryRequest request
    ) {
        IndustryResponse response = industryService.createIndustry(request);
        return ok("Create industry successfully", response);
    }

    @PutMapping("/industries/{id}")
    public ResponseEntity<ApiResponse<IndustryResponse>> updateIndustry(
            @PathVariable Long id,
            @Valid @RequestBody IndustryRequest request
    ) {
        IndustryResponse response = industryService.updateIndustry(id, request);
        return ok("Update industry successfully", response);
    }

    @PatchMapping("/industries/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeIndustryStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive
    ) {
        industryService.changeStatusIndustry(id, isActive);
        return ok("Change industry status successfully");
    }

    @DeleteMapping("/industries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIndustry(@PathVariable Long id) {
        industryService.deleteIndustry(id);
        return ok("Delete industry successfully");
    }




}
