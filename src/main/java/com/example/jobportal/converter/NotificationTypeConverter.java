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
        return s == null ? null : NotificationType.fromValue(s);
    }
}
