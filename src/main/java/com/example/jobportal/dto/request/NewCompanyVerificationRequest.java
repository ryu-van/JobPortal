package com.example.jobportal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompanyVerificationRequest {
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;


    @NotBlank(message = "Mã số thuế không được để trống")
    @Pattern(
            regexp = "^[0-9]{6}-[0-9]{3}([0-9])?$",
            message = "Mã số thuế phải đúng định dạng XXXXXX-XXX hoặc XXXXXX-XXXX"
    )
    private String taxCode;

    private String description;

    private String companySize;

    private LocalDateTime establishmentDate;

    private String contactPerson;

    @Email(message = "Email công ty không hợp lệ")
    @NotBlank(message = "Email công ty không được để trống")
    private String contactEmail;
    
    private String contactPhone;

    private String website;

    private Long industryId;

    private List<AddressRequest> addressRequest;

}
