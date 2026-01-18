package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.CompanyVerificationRequest;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompanyVerificationRequestResponse {
    private Long id;
    private String companyName;
    private String companyEmail;
    private String  address;
    private CompanyVerificationStatus status;
    private String senderName;
    private Date createdAt;

    public static CompanyVerificationRequestResponse fromEntity(CompanyVerificationRequest companyVerificationRequest) {
        String address = null;
        if (companyVerificationRequest.getAddress() != null) {
            StringBuilder sb = new StringBuilder();
            if (companyVerificationRequest.getAddress().getDetailAddress() != null) {
                sb.append(companyVerificationRequest.getAddress().getDetailAddress());
            }
            if (companyVerificationRequest.getAddress().getCommuneName() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(companyVerificationRequest.getAddress().getCommuneName());
            }
            if (companyVerificationRequest.getAddress().getProvinceName() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(companyVerificationRequest.getAddress().getProvinceName());
            }
            address = sb.toString();
        }
        CompanyVerificationRequestResponse response = new CompanyVerificationRequestResponse();
        response.id = companyVerificationRequest.getId();
        response.companyName = companyVerificationRequest.getCompanyName();
        response.companyEmail = companyVerificationRequest.getContactEmail();
        response.status = companyVerificationRequest.getStatus();
        response.senderName = companyVerificationRequest.getContactPerson();
        response.createdAt = companyVerificationRequest.getCreatedAt();
        response.address = address;
        return response;
    }
}
