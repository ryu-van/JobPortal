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
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String status;
    private LocalDateTime appliedAt;
    private String coverLetter;
    private String resumeTitle;

    public static ApplicationResponse fromEntity(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .companyName(app.getJob().getCompany() != null ? app.getJob().getCompany().getName() : null)
                .status(app.getStatus() != null ? app.getStatus().getValue() : null)
                .appliedAt(app.getAppliedAt())
                .coverLetter(app.getCoverLetter())
                .resumeTitle(app.getResume() != null ? app.getResume().getTitle() : null)
                .build();
    }
}
