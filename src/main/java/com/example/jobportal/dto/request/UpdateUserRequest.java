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
    private AddressRequest addressRequest;
    private String gender;
    private Long companyId;
    private Long roleId;
    private Boolean isActive;

}
