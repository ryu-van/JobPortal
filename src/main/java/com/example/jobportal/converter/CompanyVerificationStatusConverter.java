package com.example.jobportal.converter;

import com.example.jobportal.model.enums.CompanyVerificationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.lang.annotation.Annotation;
import java.util.Arrays;

@Converter(autoApply = true)
public class CompanyVerificationStatusConverter implements AttributeConverter<CompanyVerificationStatus, String> {

    @Override
    public String convertToDatabaseColumn(CompanyVerificationStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public CompanyVerificationStatus convertToEntityAttribute(String s) {
        if (s == null) return null;
        return Arrays.stream(CompanyVerificationStatus.values())
                .filter(d -> d.getValue().equalsIgnoreCase(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + s));
    }

}
