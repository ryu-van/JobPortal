package com.example.jobportal.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompanyVerificationStatus {
    PENDING("pending", "Đang chờ duyệt"),
    APPROVED("approved", "Đã duyệt"),
    REJECTED("rejected", "Bị từ chối");

    private final String value;
    private final String displayName;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static CompanyVerificationStatus fromValue(String value) {
        for (CompanyVerificationStatus status : CompanyVerificationStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
