package com.example.jobportal.model.enums;

public enum ExperienceLevel {
    INTERN("intern"),
    FRESHER("fresher"),
    JUNIOR("junior"),
    MID("mid"),
    SENIOR("senior"),
    LEAD("lead");

    private final String value;

    ExperienceLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExperienceLevel fromValue(String value) {
        for (ExperienceLevel item : values()) {
            if (item.value.equalsIgnoreCase(value) || item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid experience level: " + value);
    }
}
