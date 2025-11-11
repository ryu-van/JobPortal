package com.example.jobportal.dto.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicationRequest {
    private Long jobId;
    private Long resumeId;
    private String coverLetter;
}
