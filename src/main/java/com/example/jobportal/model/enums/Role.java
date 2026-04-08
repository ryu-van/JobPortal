package com.example.jobportal.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ROLE_ADMIN(1, "Quản trị hệ thống"),
    ROLE_HR(3, "Tuyển dụng"),
    ROLE_CANDIDATE(4, "Ứng viên"),
    ROLE_COMPANY_ADMIN(2, "Quản trị doanh nghiệp");

    private final int id;
    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static Role fromId(int id) {
        for (Role role : Role.values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role id: " + id);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
