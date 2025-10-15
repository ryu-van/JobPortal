package com.example.jobportal.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.convert.ReadingConverter;

@Getter
@RequiredArgsConstructor
public enum JobStatus {
    DRAFT("draft","Nháp"),
    PUBLISHED("published","Đang tuyển"),
    CLOSED("closed","Đã đóng"),
    ARCHIVED("archived","Lưu trữ");

    private final String value;
    private final String displayName;

    @JsonValue
    public String getValue() {
        return value;
    }
    public static JobStatus fromValue(String value) {
        for (JobStatus status : JobStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid job status: " + value);
    }

}
