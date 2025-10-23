package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserBaseResponse {
    private Long id;
    private String fullName;
    private String code;
    private String email;
    private Boolean gender;
    private String roleName;
    private boolean isActive;
}
