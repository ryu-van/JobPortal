package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResumeDetailResponse {
    private Long id;

    private String title;

    private String fileUrl;

    private String fileName;

    private String fileType;

    private String summary;

    private Boolean isPrimary = false;
    private Boolean isPublic = false;

    private UserBaseResponse user;

    private List<ResumeEducationResponse> educations = new ArrayList<>();

    private List<ResumeExperienceResponse> experiences = new ArrayList<>();

    private List<ResumeSkillResponse> skills = new ArrayList<>();

    public static ResumeDetailResponse fromEntity(Resume resume) {
        return ResumeDetailResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .fileUrl(resume.getFileUrl())
                .fileName(resume.getFileName())
                .fileType(resume.getFileType())
                .summary(resume.getSummary())
                .isPrimary(resume.getIsPrimary())
                .isPublic(resume.getIsPublic())
                .user(UserBaseResponse.fromEntity(resume.getUser()))
                .educations(resume.getEducations().stream()
                        .map(ResumeEducationResponse::fromEntity)
                        .collect(Collectors.toList()))
                .experiences(resume.getExperiences().stream()
                        .map(ResumeExperienceResponse::fromEntity)
                        .collect(Collectors.toList()))
                .skills(resume.getSkills().stream()
                        .map(ResumeSkillResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
