package com.example.jobportal.converter;

import com.example.jobportal.model.enums.JobStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JobStatusConverter implements AttributeConverter<JobStatus, String> {
    @Override
    public String convertToDatabaseColumn(JobStatus jobStatus) {
        return jobStatus != null ? jobStatus.getValue() : null;
    }

    @Override
    public JobStatus convertToEntityAttribute(String s) {
        return s != null ? JobStatus.fromValue(s) : null;
    }
}
