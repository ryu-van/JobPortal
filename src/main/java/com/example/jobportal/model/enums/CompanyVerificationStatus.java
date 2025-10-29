package com.example.jobportal.model.enums;

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

}
