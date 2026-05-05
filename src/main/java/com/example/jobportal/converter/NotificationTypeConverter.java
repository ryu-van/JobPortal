package com.example.jobportal.converter;

import com.example.jobportal.model.enums.NotificationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {
    @Override
    public String convertToDatabaseColumn(NotificationType type) {
        return type == null ? null : type.getValue();
    }

    @Override
    public NotificationType convertToEntityAttribute(String s) {
        if (s == null) return null;
        try {
            return NotificationType.fromValue(s);
        } catch (IllegalArgumentException e) {
            return null; // unknown value → treat as null instead of 500
        }
    }
}
