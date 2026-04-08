package com.example.jobportal.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompanyRequest {
    private String name;

    private String email;

    private String taxCode;

    private LocalDate establishmentDate;

    private String description;

    private Long industryId;

    private String companySize;

    private String website;

    private String logoUrl;

    private List<AddressRequest> addressRequest;

    private Boolean isVerified = false;

    private Boolean isActive = true;
}
