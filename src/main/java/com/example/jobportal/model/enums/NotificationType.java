package com.example.jobportal.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    APPLICATION_SUBMITTED("application_submitted", "Đã nộp đơn ứng tuyển"),
    APPLICATION_STATUS_UPDATE("application_status_update", "Cập nhật trạng thái ứng tuyển"),
    JOB_EXPIRED("job_expired", "Tin tuyển dụng hết hạn"),
    JOB_CREATED("job_created", "Tin tuyển dụng mới"),
    JOB_UPDATED("job_updated", "Cập nhật tin tuyển dụng"),
    JOB_STATUS_CHANGED("job_status_changed", "Trạng thái tin tuyển dụng thay đổi"),
    JOB_PUBLISHED("job_published", "Tin tuyển dụng đã đăng"),
    JOB_SAVED("job_saved", "Đã lưu tin tuyển dụng"),
    CATEGORY_CREATED("category_created", "Danh mục mới"),
    CATEGORY_UPDATED("category_updated", "Cập nhật danh mục"),
    CATEGORY_DELETED("category_deleted", "Xóa danh mục"),
    COMPANY_VERIFY_REQUESTED("company_verify_requested", "Yêu cầu xác thực doanh nghiệp"),
    COMPANY_VERIFIED("company_verified", "Doanh nghiệp đã xác thực"),
    COMPANY_REJECTED("company_rejected", "Yêu cầu xác thực bị từ chối"),
    COMPANY_STATUS_CHANGED("company_status_changed", "Trạng thái doanh nghiệp thay đổi"),
    RESUME_CREATED("resume_created", "Hồ sơ mới"),
    RESUME_UPDATED("resume_updated", "Cập nhật hồ sơ"),
    RESUME_DELETED("resume_deleted", "Xóa hồ sơ"),
    RESUME_PRIMARY_CHANGED("resume_primary_changed", "Thay đổi hồ sơ chính"),
    RESUME_VISIBILITY_CHANGED("resume_visibility_changed", "Thay đổi hiển thị hồ sơ"),
    SYSTEM("system", "Hệ thống");

    private final String value;
    private final String displayName;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static NotificationType fromValue(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + value);
    }
}
