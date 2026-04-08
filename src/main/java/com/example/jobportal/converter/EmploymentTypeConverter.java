package com.example.jobportal.converter;

import com.example.jobportal.model.enums.EmploymentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EmploymentTypeConverter implements AttributeConverter<EmploymentType, String> {

    @Override
    public String convertToDatabaseColumn(EmploymentType attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public EmploymentType convertToEntityAttribute(String dbData) {
        return dbData != null ? EmploymentType.fromValue(dbData) : null;
    }
}