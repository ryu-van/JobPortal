package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.ResumeExperience;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResumeExperienceResponse {

    private Long id;

    private String companyName;

    private String position;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isCurrent = false;

    private Integer displayOrder;
    public static ResumeExperienceResponse fromEntity(ResumeExperience resumeExperience) {
        return ResumeExperienceResponse.builder()
                .id(resumeExperience.getId())
                .companyName(resumeExperience.getCompanyName())
                .position(resumeExperience.getPosition())
                .description(resumeExperience.getDescription())
                .startDate(resumeExperience.getStartDate())
                .endDate(resumeExperience.getEndDate())
                .isCurrent(resumeExperience.getIsCurrent())
                .displayOrder(resumeExperience.getDisplayOrder())
                .build();
    }
}
