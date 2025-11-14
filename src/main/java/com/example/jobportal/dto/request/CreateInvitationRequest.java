package com.example.jobportal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvitationRequest {

    @Email(message = "Email không hợp lệ")
    private String email;

    @Min(value = 1, message = "Số lần sử dụng tối thiểu là 1")
    @Max(value = 100, message = "Số lần sử dụng tối đa là 100")
    private Integer maxUses = 1;

    @Min(value = 1, message = "Thời hạn tối thiểu là 1 giờ")
    @Max(value = 720, message = "Thời hạn tối đa là 720 giờ (30 ngày)")
    private Integer expiresInHours = 72;
}

