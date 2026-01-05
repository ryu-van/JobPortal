package com.example.jobportal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.jobportal.model.entity.CompanyVerificationRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private List<DocumentFileResponse> documentFiles;

    private String status ;

    private String adminNotes;

    private LocalDateTime reviewedAt;

    private AddressResponse addressResponse;

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
        companyVerificationRequestDetailResponse.setStatus(companyVerificationRequest.getStatus().toString());
        companyVerificationRequestDetailResponse.setAdminNotes(companyVerificationRequest.getAdminNotes());
        companyVerificationRequestDetailResponse.setReviewedAt(companyVerificationRequest.getReviewedAt());
        companyVerificationRequestDetailResponse.setAddressResponse(AddressResponse.fromEntity(companyVerificationRequest.getAddress()));
        companyVerificationRequestDetailResponse.setDocumentFiles(
                companyVerificationRequest.getDocumentFiles() != null
                        ? companyVerificationRequest.getDocumentFiles().stream()
                        .map(df -> DocumentFileResponse.builder()
                                .fileName(df.getFileName())
                                .url(df.getUrl())
                                .publicId(df.getPublicId())
                                .contentType(df.getContentType())
                                .fileSize(df.getFileSize())
                                .uploadedAt(df.getUploadedAt())
                                .build())
                        .toList()
                        : null
        );

        companyVerificationRequestDetailResponse.setUserName(companyVerificationRequest.getUser().getFullName());
        if (companyVerificationRequest.getReviewedBy() != null) {
            companyVerificationRequestDetailResponse.setReviewedByName(companyVerificationRequest.getReviewedBy().getFullName());
            companyVerificationRequestDetailResponse.setReviewedByEmail(companyVerificationRequest.getReviewedBy().getEmail());
        }
        return companyVerificationRequestDetailResponse;
    }

}
