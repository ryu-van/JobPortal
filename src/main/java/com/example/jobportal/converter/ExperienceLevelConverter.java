package com.example.jobportal.converter;

import com.example.jobportal.model.enums.ExperienceLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ExperienceLevelConverter implements AttributeConverter<ExperienceLevel, String> {

    @Override
    public String convertToDatabaseColumn(ExperienceLevel attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public ExperienceLevel convertToEntityAttribute(String dbData) {
        return dbData != null ? ExperienceLevel.fromValue(dbData) : null;
    }
}
