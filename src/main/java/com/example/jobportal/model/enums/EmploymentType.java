package com.example.jobportal.model.enums;

public enum EmploymentType {
    FULL_TIME("full_time"),
    PART_TIME("part_time"),
    INTERN("intern"),
    CONTRACT("contract"),
    FREELANCE("freelance");

    private final String value;

    EmploymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EmploymentType fromValue(String value) {
        for (EmploymentType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid employment type: " + value);
    }
}
