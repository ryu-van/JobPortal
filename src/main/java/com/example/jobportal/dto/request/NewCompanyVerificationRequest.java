package com.example.jobportal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompanyVerificationRequest {
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    @NotBlank(message = "Mã giấy phép kinh doanh không được để trống")
    private String businessLicense;

    @NotBlank(message = "Mã số thuế không được để trống")
    @Pattern(regexp = "^[0-9]{10}(-[0-9]{3})?$", message = "Mã số thuế không hợp lệ")
    private String taxCode;

    private String contactPerson;

    @Email(message = "Email công ty không hợp lệ")
    @NotBlank(message = "Email công ty không được để trống")
    private String contactEmail;
    private String contactPhone;

    private AddressRequest addressRequest;

}
