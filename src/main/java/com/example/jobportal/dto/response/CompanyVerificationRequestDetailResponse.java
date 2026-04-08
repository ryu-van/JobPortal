package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.CompanyVerificationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<String> documents;

    private String requestedRole;

    private String status ;

    private String adminNotes;

    private LocalDateTime reviewedAt;

    private List<AddressResponse> addresses;

    private String userName;

    private String logoUrl;

    private String reviewedByName;

    private String reviewedByEmail;

    public static CompanyVerificationRequestDetailResponse fromEntity(CompanyVerificationRequest companyVerificationRequest){
        CompanyVerificationRequestDetailResponse companyVerificationRequestDetailResponse = new CompanyVerificationRequestDetailResponse();
        companyVerificationRequestDetailResponse.setId(companyVerificationRequest.getId());
        companyVerificationRequestDetailResponse.setTaxCode(companyVerificationRequest.getTaxCode());
        companyVerificationRequestDetailResponse.setName(companyVerificationRequest.getCompanyName());
        companyVerificationRequestDetailResponse.setEmail(companyVerificationRequest. getContactEmail());
        companyVerificationRequestDetailResponse.setContactPerson(companyVerificationRequest.getContactPerson());
        companyVerificationRequestDetailResponse.setContactEmail(companyVerificationRequest.getContactEmail());
        companyVerificationRequestDetailResponse.setContactPhone(companyVerificationRequest.getContactPhone());
        companyVerificationRequestDetailResponse.setDocuments(companyVerificationRequest.getDocumentUrls());
        companyVerificationRequestDetailResponse.setStatus(companyVerificationRequest.getStatus() != null ? companyVerificationRequest.getStatus().getValue() : null);
        companyVerificationRequestDetailResponse.setAdminNotes(companyVerificationRequest.getAdminNotes());
        companyVerificationRequestDetailResponse.setReviewedAt(companyVerificationRequest.getReviewedAt());
        companyVerificationRequestDetailResponse.setAddresses(
                companyVerificationRequest.getAddresses() != null
                        ? companyVerificationRequest.getAddresses().stream()
                        .map(AddressResponse::fromEntity)
                        .toList()
                        : List.of()
        );
        companyVerificationRequestDetailResponse.setUserName(companyVerificationRequest.getUser() != null ? companyVerificationRequest.getUser().getFullName() : null);
        companyVerificationRequestDetailResponse.setLogoUrl(companyVerificationRequest.getLogoUrl());
        companyVerificationRequestDetailResponse.setReviewedByName(companyVerificationRequest.getReviewedBy() != null ? companyVerificationRequest.getReviewedBy().getFullName() : null);
        companyVerificationRequestDetailResponse.setReviewedByEmail(companyVerificationRequest.getReviewedBy() != null ? companyVerificationRequest.getReviewedBy().getEmail() : null);
        return companyVerificationRequestDetailResponse;
    }

}
