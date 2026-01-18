package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.CompanyVerificationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CompanyVerificationRequestDetailResponse {

    private Long id;

    private String businessLicense;

    private String taxCode;

    private String name;

    private String email;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    private String documents;

    private String requestedRole;

    private String status ;

    private String adminNotes;

    private LocalDateTime reviewedAt;

    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;

    private String userName;

    private String reviewedByName;

    private String reviewedByEmail;

    public static CompanyVerificationRequestDetailResponse fromEntity(CompanyVerificationRequest companyVerificationRequest){
        CompanyVerificationRequestDetailResponse companyVerificationRequestDetailResponse = new CompanyVerificationRequestDetailResponse();
        companyVerificationRequestDetailResponse.setId(companyVerificationRequest.getId());
        companyVerificationRequestDetailResponse.setBusinessLicense(companyVerificationRequest.getBusinessLicense());
        companyVerificationRequestDetailResponse.setTaxCode(companyVerificationRequest.getTaxCode());
        companyVerificationRequestDetailResponse.setName(companyVerificationRequest.getCompanyName());
        companyVerificationRequestDetailResponse.setEmail(companyVerificationRequest. getContactEmail());
        companyVerificationRequestDetailResponse.setContactPerson(companyVerificationRequest.getContactPerson());
        companyVerificationRequestDetailResponse.setContactEmail(companyVerificationRequest.getContactEmail());
        companyVerificationRequestDetailResponse.setContactPhone(companyVerificationRequest.getContactPhone());
        companyVerificationRequestDetailResponse.setDocuments(String.join(",", companyVerificationRequest.getDocumentUrls()));
        companyVerificationRequestDetailResponse.setStatus(companyVerificationRequest.getStatus().toString());
        companyVerificationRequestDetailResponse.setAdminNotes(companyVerificationRequest.getAdminNotes());
        companyVerificationRequestDetailResponse.setReviewedAt(companyVerificationRequest.getReviewedAt());
        companyVerificationRequestDetailResponse.setStreet(companyVerificationRequest.getAddress() != null ? companyVerificationRequest.getAddress().getDetailAddress() : null);
        companyVerificationRequestDetailResponse.setWard(companyVerificationRequest.getAddress() != null ? companyVerificationRequest.getAddress().getCommuneName() : null);
        companyVerificationRequestDetailResponse.setCity(companyVerificationRequest.getAddress() != null ? companyVerificationRequest.getAddress().getProvinceName() : null);
        companyVerificationRequestDetailResponse.setUserName(companyVerificationRequest.getUser().getFullName());
        companyVerificationRequestDetailResponse.setReviewedByName(companyVerificationRequest.getReviewedBy().getFullName());
        companyVerificationRequestDetailResponse.setReviewedByEmail(companyVerificationRequest.getReviewedBy().getEmail());
        return companyVerificationRequestDetailResponse;
    }

}
