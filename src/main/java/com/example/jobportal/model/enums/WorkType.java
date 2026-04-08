package com.example.jobportal.model.enums;

public enum WorkType {
    REMOTE("remote"),
    ONSITE("onsite"),
    HYBRID("hybrid");

    private final String value;

    WorkType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WorkType fromValue(String value) {
        for (WorkType item : values()) {
            if (item.value.equalsIgnoreCase(value) || item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid work type: " + value);
    }
}
