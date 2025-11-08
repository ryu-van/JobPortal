package com.example.jobportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumeRequest {
    private Long userId;
    private String title;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private String summary;
    private Boolean isPrimary;
    private Boolean isPublic;
    private List<EducationRequest> educations;
    private List<ExperienceRequest> experiences;
    private List<SkillRequest> skills;
}
