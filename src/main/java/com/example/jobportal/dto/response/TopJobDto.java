package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for top job statistics
 * Used in dashboard statistics to show jobs with highest application counts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopJobDto {
    private Long jobId;
    private String jobTitle;
    private Long applicationCount;
    private LocalDateTime publishedAt;
}
