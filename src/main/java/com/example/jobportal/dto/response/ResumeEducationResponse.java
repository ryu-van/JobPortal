package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Resume;
import com.example.jobportal.model.entity.ResumeEducation;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResumeEducationResponse {

    private Long id;

    private String institution;

    private String degree;

    private String fieldOfStudy;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal gpa;

    private String description;

    private Integer displayOrder;

    public static ResumeEducationResponse fromEntity(ResumeEducation resumeEducation) {
        return ResumeEducationResponse.builder()
                .id(resumeEducation.getId())
                .institution(resumeEducation.getInstitution())
                .degree(resumeEducation.getDegree())
                .fieldOfStudy(resumeEducation.getFieldOfStudy())
                .startDate(resumeEducation.getStartDate())
                .endDate(resumeEducation.getEndDate())
                .gpa(resumeEducation.getGpa())
                .description(resumeEducation.getDescription())
                .displayOrder(resumeEducation.getDisplayOrder())
                .build();
    }

}
