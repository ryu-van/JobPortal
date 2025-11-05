package com.example.jobportal.controller;

import com.example.jobportal.dto.request.NewCompanyVerificationRequest;
import com.example.jobportal.dto.request.UpdateCompanyRequest;
import com.example.jobportal.dto.response.*;
import com.example.jobportal.service.CompanyService;
import com.example.jobportal.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/companies")
public class CompanyController extends BaseController {

    private final CompanyService companyService;
    private FileUploadService fileUploadService;

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<CompanyVerificationRequestResponse>>> getRequestsToCreateCompany(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String verifyStatus,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate createdDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CompanyVerificationRequestResponse> companyPage =
                companyService.getAllCompanyVerificationRequest(keyword, verifyStatus, createdDate, pageable);

        PageInfo pageInfo = PageInfo.of(companyPage.getNumber(), companyPage.getSize(), companyPage.getTotalElements());
        return ok("Get company verification requests successfully", companyPage.getContent(), pageInfo);
    }


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

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<CompanyVerificationRequestResponse>> createCompanyVerificationRequest(
            @RequestBody NewCompanyVerificationRequest request
    ) {
        CompanyVerificationRequestResponse response = companyService.createCompanyVerificationRequest(request);
        return ok("Create company verification request successfully", response);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyBaseResponse>> getCompanyDetail(@PathVariable Long companyId) {
        CompanyBaseResponse response = companyService.getCompanyById(companyId);
        return ok("Get company detail successfully", response);
    }

    @GetMapping("/requests/detail/{requestId}")
    public ResponseEntity<ApiResponse<CompanyVerificationRequestDetailResponse>> getCompanyVerificationRequestDetail(
            @PathVariable Long requestId
    ) {
        CompanyVerificationRequestDetailResponse response = companyService.getCompanyVerificationRequestById(requestId);
        return ok("Get company verification request detail successfully", response);
    }


    @GetMapping("/requests/company/{companyId}")
    public ResponseEntity<ApiResponse<CompanyVerificationRequestDetailResponse>> getCompanyVerificationRequestByCompanyId(
            @PathVariable Long companyId
    ) {
        CompanyVerificationRequestDetailResponse response =
                companyService.getCompanyVerificationRequestByCompanyId(companyId);
        return ok("Get company verification request by company id successfully", response);
    }

    @PutMapping("/{companyId}/status")
    public ResponseEntity<ApiResponse<Void>> changeCompanyStatus(
            @PathVariable Long companyId,
            @RequestParam Boolean isActive
    ) {
        companyService.changeCompanyStatus(companyId, isActive);
        return ok("Change company status successfully");
    }
    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyBaseResponse>> updateCompany(
            @PathVariable Long companyId,
            @RequestBody UpdateCompanyRequest companyRequest, MultipartFile file){
        String logoUrl = fileUploadService.uploadFile(file);
        companyRequest.setLogoUrl(logoUrl);
        CompanyBaseResponse response = companyService.updateCompany(companyId, companyRequest);
        return ok("Update company successfully", response);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return ok("Delete company successfully");
    }


    @PutMapping("/requests/{requestId}/review")
    public ResponseEntity<ApiResponse<Void>> reviewCompanyVerificationRequest(
            @PathVariable Long requestId,
            @RequestParam Long reviewedById,
            @RequestParam boolean isApproved,
            @RequestParam(required = false) String reason
    ) {
        companyService.reviewCompanyVerificationRequest(requestId, reviewedById, isApproved, reason);
        return ok("Review company verification request successfully");
    }
}
