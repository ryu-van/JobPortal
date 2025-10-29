package com.example.jobportal.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompanyRequest {
    private String name;

    private String email;

    private String description;

    private String industry;

    private String companySize;

    private String website;

    private String logoUrl;

    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;

    private Boolean isVerified = false;

    private Boolean isActive = true;
}
