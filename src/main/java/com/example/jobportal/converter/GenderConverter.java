package com.example.jobportal.converter;

import com.example.jobportal.model.enums.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {
    @Override
    public String convertToDatabaseColumn(Gender gender) {
        return gender == null ? null : gender.getValue();
    }

    @Override
    public Gender convertToEntityAttribute(String s) {
        return s == null ? null : Gender.fromValue(s);
    }
}
