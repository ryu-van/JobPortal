package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


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
                .build();
    }
}
