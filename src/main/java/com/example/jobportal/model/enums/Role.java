package com.example.jobportal.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ROLE_ADMIN(1, "Quản trị hệ thống"),
    ROLE_HR(2, "Tuyển dụng"),
    ROLE_CANDIDATE(3, "Ứng viên"),
    ROLE_COMPANY_ADMIN(4, "Quản trị doanh nghiệp");

    private final int id;
    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
