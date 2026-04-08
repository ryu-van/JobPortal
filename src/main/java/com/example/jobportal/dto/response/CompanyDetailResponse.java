package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Company;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDetailResponse {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private String description;

    private Long industryId;

    private LocalDateTime establishmentDate;

    private String taxCode;

    private String companySize;

    private String website;


    private String logoUrl;

    private String logoPublicId;

    private Boolean isVerified;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private List<AddressResponse> addresses;

    public static CompanyDetailResponse fromEntity(Company company) {
        return CompanyDetailResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .phoneNumber(company.getPhoneNumber())
                .description(company.getDescription())
                .industryId(company.getIndustry().getId())
                .establishmentDate(company.getEstablishmentDate())
                .taxCode(company.getTaxCode())
                .companySize(company.getCompanySize())
                .website(company.getWebsite())
                .logoUrl(company.getLogoUrl())
                .logoPublicId(company.getLogoPublicId())
                .isActive(company.getIsActive())
                .createdAt(company.getCreatedAt())
                .addresses(
                        company.getAddresses() != null
                                ? company.getAddresses().stream()
                                .sorted((a, b) -> Boolean.compare(
                                        Boolean.TRUE.equals(b.getIsPrimary()),
                                        Boolean.TRUE.equals(a.getIsPrimary())
                                ))
                                .map(AddressResponse::fromEntity)
                                .collect(Collectors.toList())
                                : Collections.emptyList()
                )
                .build();
    }
}
