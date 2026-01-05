package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDetailResponse extends UserBaseResponse{
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private AddressResponse address;
    private String avatarUrl;
    private Boolean isEmailVerified;
    private LocalDateTime lastLoginAt;
    private Long companyId;
    private String companyName;

    public static UserDetailResponse fromEntity(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .code(user.getCode())
                .email(user.getEmail())
                .gender(user.getGender())
                .roleId(user.getRole().getId())
                .roleName(user.getRole().getName())
                .address(AddressResponse.fromEntity(user.getAddress()))
                .isActive(user.getIsActive())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .isEmailVerified(user.getIsEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .build();
    }

}
