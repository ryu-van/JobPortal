package com.example.jobportal.converter;

import com.example.jobportal.model.enums.WorkType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class WorkTypeConverter implements AttributeConverter<WorkType, String> {

    @Override
    public String convertToDatabaseColumn(WorkType attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public WorkType convertToEntityAttribute(String dbData) {
        return dbData != null ? WorkType.fromValue(dbData) : null;
    }
}
