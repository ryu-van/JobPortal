package com.example.jobportal.dto.response;


import com.example.jobportal.model.entity.Resume;
import lombok.*;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResumeBaseResponse {
    private Long id;
    private String title;
    private   Boolean isPrimary;
    private Boolean isPublic;
    private Date createdAt;
    public static ResumeBaseResponse fromEntity(Resume resume) {
        return ResumeBaseResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .isPrimary(resume.getIsPrimary())
                .isPublic(resume.getIsPublic())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
