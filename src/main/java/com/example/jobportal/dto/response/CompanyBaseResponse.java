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
    private String city;
    private String country;

    public static CompanyBaseResponse fromEntity(Company company) {
        return CompanyBaseResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .isVerified(company.getIsVerified())
                .isActive(company.getIsActive())
                .industry(company.getIndustry())
                .companySize(company.getCompanySize())
                .city(company.getAddress() != null ? company.getAddress().getCity() : null)
                .country(company.getAddress() != null ? company.getAddress().getCountry() : null)
                .build();
    }

}
