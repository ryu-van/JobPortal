package com.example.jobportal.model.enums;



public enum ApplicationStatus {
    PENDING,       // Ứng viên mới nộp
    REVIEWED,      // Nhà tuyển dụng đã xem
    ACCEPTED,      // Được chấp nhận (qua vòng đầu, phỏng vấn)
    REJECTED,      // Bị từ chối
    HIRED,         // Đã được nhận
    WITHDRAWN;     // Ứng viên rút hồ sơ

    public static ApplicationStatus fromString(String value) {
        try {
            return ApplicationStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid application status: " + value);
        }
    }
}
