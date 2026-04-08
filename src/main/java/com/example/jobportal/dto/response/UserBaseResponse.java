package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


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
    private Gender gender;
    private Long roleId;
    private String roleName;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime tokenExpiryDate;
    private String phoneNumber;
    private String avatarUrl;
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
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
