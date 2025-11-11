package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Application;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private Long id;
    private String jobTitle;
    private String status;
    private LocalDateTime appliedAt;
    private String coverLetter;
    private String resumeTitle;

    public static ApplicationResponse fromEntity(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobTitle(app.getJob().getTitle())
                .status(String.valueOf(app.getStatus()))
                .appliedAt(app.getAppliedAt())
                .coverLetter(app.getCoverLetter())
                .resumeTitle(app.getResume() != null ? app.getResume().getTitle() : null)
                .build();
    }
}
