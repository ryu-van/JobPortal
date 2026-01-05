package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBaseResponse {
    private Long id;
    private String name;
    private String email;
    private Boolean isVerified;
    private Boolean isActive;
    private String industry;
    private String companySize;
    private AddressResponse address;

    public CompanyBaseResponse(Long id, String name, String email, Boolean isVerified, Boolean isActive, String industry, String companySize, String provinceName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isVerified = isVerified;
        this.isActive = isActive;
        this.industry = industry;
        this.companySize = companySize;
        if (provinceName != null) {
            this.address = AddressResponse.builder()
                    .provinceName(provinceName)
                    .build();
        }
    }

    public static CompanyBaseResponse fromEntity(Company company) {
        
        return CompanyBaseResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .isVerified(company.getIsVerified())
                .isActive(company.getIsActive())
                .industry(company.getIndustry())
                .companySize(company.getCompanySize())
                .address(company.getAddresses() != null
                        ? company.getAddresses().stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                        .findFirst()
                        .map(AddressResponse::fromEntity)
                        .orElse(null)
                        : null)
                .build();
    }

}
