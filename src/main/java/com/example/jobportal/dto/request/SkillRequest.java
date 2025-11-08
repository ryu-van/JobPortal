package com.example.jobportal.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRequest {
    private Long id;
    private String skillName;
    private String proficiencyLevel;
    private Integer yearsOfExperience;
}
