package com.example.jobportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    private String phoneNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;
    private String avatarUrl;
    private Boolean gender;
    private Long CompanyId;
    private Long roleId;
    private Boolean isActive;

}
