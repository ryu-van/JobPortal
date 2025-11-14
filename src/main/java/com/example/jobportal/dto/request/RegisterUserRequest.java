package com.example.jobportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {
    private String fullName;
    private String email;
    private String password;
    private Boolean gender;
    private Long roleId;
    private String codeInvitation;

}
