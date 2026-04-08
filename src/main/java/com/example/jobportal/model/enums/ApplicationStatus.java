package com.example.jobportal.model.enums;



import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    PENDING("pending", "Đang chờ duyệt"),
    REVIEWED("reviewed", "Đã xem"),
    ACCEPTED("accepted", "Chấp nhận"),
    REJECTED("rejected", "Từ chối"),
    HIRED("hired", "Đã tuyển"),
    WITHDRAWN("withdrawn", "Đã rút đơn");

    private final String value;
    private final String displayName;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ApplicationStatus fromValue(String value) {
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid application status: " + value);
    }
}
