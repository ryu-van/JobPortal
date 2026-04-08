package com.example.jobportal.converter;

import com.example.jobportal.model.enums.ApplicationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApplicationStatusConverter implements AttributeConverter<ApplicationStatus, String> {
    @Override
    public String convertToDatabaseColumn(ApplicationStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public ApplicationStatus convertToEntityAttribute(String s) {
        return s == null ? null : ApplicationStatus.fromValue(s);
    }
}
