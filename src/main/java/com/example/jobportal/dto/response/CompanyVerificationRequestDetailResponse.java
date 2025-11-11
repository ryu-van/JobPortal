package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.BaseAddress;
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
        companyVerificationRequestDetailResponse.setDocuments(companyVerificationRequest.getDocuments());
        companyVerificationRequestDetailResponse.setRequestedRole(companyVerificationRequest.getRequestedRole());
        companyVerificationRequestDetailResponse.setStatus(companyVerificationRequest.getStatus().toString());
        companyVerificationRequestDetailResponse.setAdminNotes(companyVerificationRequest.getAdminNotes());
        companyVerificationRequestDetailResponse.setReviewedAt(companyVerificationRequest.getReviewedAt());
        companyVerificationRequestDetailResponse.setStreet(companyVerificationRequest.getAddress().getStreet());
        companyVerificationRequestDetailResponse.setWard(companyVerificationRequest.getAddress().getWard());
        companyVerificationRequestDetailResponse.setDistrict(companyVerificationRequest.getAddress().getDistrict());
        companyVerificationRequestDetailResponse.setCity(companyVerificationRequest.getAddress().getCity());
        companyVerificationRequestDetailResponse.setCountry(companyVerificationRequest.getAddress().getCountry());
        companyVerificationRequestDetailResponse.setUserName(companyVerificationRequest.getUser().getFullName());
        companyVerificationRequestDetailResponse.setReviewedByName(companyVerificationRequest.getReviewedBy().getFullName());
        companyVerificationRequestDetailResponse.setReviewedByEmail(companyVerificationRequest.getReviewedBy().getEmail());
        return companyVerificationRequestDetailResponse;
    }

}
