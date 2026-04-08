package com.example.jobportal.converter;

import com.example.jobportal.model.enums.CompanyVerificationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CompanyVerificationStatusConverter implements AttributeConverter<CompanyVerificationStatus, String> {

    @Override
    public String convertToDatabaseColumn(CompanyVerificationStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public CompanyVerificationStatus convertToEntityAttribute(String s) {
        return s == null ? null : CompanyVerificationStatus.fromValue(s);
    }

}
