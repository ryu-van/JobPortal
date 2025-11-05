package com.example.jobportal.service;

import com.example.jobportal.dto.request.NewCompanyVerificationRequest;
import com.example.jobportal.dto.request.UpdateCompanyRequest;
import com.example.jobportal.dto.response.CompanyBaseResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestDetailResponse;
import com.example.jobportal.dto.response.CompanyVerificationRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Date;

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

}
