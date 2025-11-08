package com.example.jobportal.dto.response;


import com.example.jobportal.model.entity.ResumeSkill;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResumeSkillResponse {

    private Long id;

    private String skillName;

    private String proficiencyLevel;

    private Integer yearsOfExperience;

    public static ResumeSkillResponse fromEntity(ResumeSkill resumeSkill) {
        return ResumeSkillResponse.builder()
                .id(resumeSkill.getId())
                .skillName(resumeSkill.getSkillName())
                .proficiencyLevel(resumeSkill.getProficiencyLevel())
                .yearsOfExperience(resumeSkill.getYearsOfExperience())
                .build();
    }

}
