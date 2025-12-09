package com.example.jobportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private LocalDate dateOfBirth;
    private String fullName;
    private String phoneNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;
    private Boolean gender;
    private Long CompanyId;
    private Long roleId;
    private Boolean isActive;

}
