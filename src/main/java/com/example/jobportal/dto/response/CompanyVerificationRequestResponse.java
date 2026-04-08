package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Address;
import com.example.jobportal.model.entity.CompanyVerificationRequest;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompanyVerificationRequestResponse {
    private Long id;
    private String companyName;
    private String companyEmail;
    private String address;
    private CompanyVerificationStatus status;
    private String senderName;
    private String logoUrl;
    private LocalDateTime createdAt;

    public CompanyVerificationRequestResponse(
            Long id,
            String companyName,
            String companyEmail,
            String address,
            CompanyVerificationStatus status,
            String senderName,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.companyName = companyName;
        this.companyEmail = companyEmail;
        this.address = address;
        this.status = status;
        this.senderName = senderName;
        this.createdAt = createdAt;
    }

    public static CompanyVerificationRequestResponse fromEntity(CompanyVerificationRequest companyVerificationRequest) {
        String addressStr = null;
        if (companyVerificationRequest.getAddresses() != null && !companyVerificationRequest.getAddresses().isEmpty()) {
            Address primaryAddress = companyVerificationRequest.getAddresses().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                    .findFirst()
                    .orElse(companyVerificationRequest.getAddresses().iterator().next());

            StringBuilder sb = new StringBuilder();
            if (primaryAddress.getDetailAddress() != null) {
                sb.append(primaryAddress.getDetailAddress());
            }
            if (primaryAddress.getCommuneName() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(primaryAddress.getCommuneName());
            }
            if (primaryAddress.getProvinceName() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(primaryAddress.getProvinceName());
            }
            addressStr = sb.toString();
        }

        CompanyVerificationRequestResponse response = new CompanyVerificationRequestResponse();
        response.id = companyVerificationRequest.getId();
        response.companyName = companyVerificationRequest.getCompanyName();
        response.companyEmail = companyVerificationRequest.getContactEmail();
        response.status = companyVerificationRequest.getStatus();
        response.senderName = companyVerificationRequest.getContactPerson();
        response.logoUrl = companyVerificationRequest.getLogoUrl();
        response.createdAt = companyVerificationRequest.getCreatedAt();
        response.address = addressStr;
        return response;
    }
}