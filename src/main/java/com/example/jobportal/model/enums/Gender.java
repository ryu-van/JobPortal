package com.example.jobportal.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("male", "Nam"),
    FEMALE("female", "Nữ"),
    OTHER("other", "Khác"),
    PREFER_NOT_TO_SAY("prefer_not_to_say", "Không muốn tiết lộ");

    private final String value;
    private final String displayName;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Gender fromValue(String value) {
        if (value == null) return null;
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(value) || gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + value);
    }
}
