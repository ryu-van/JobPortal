package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Date;


@SuperBuilder
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
    private Long roleId;
    private String roleName;
    private boolean isActive;
    private boolean isEmailVerified;
    private Date tokenExpiryDate;
    private String phoneNumber;
    public static UserBaseResponse fromEntity(User user) {
        return UserBaseResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .code(user.getCode())
                .email(user.getEmail())
                .gender(user.getGender())
                .roleId(user.getRole().getId())
                .roleName(user.getRole().getName())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .tokenExpiryDate(user.getTokenExpiryDate())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
