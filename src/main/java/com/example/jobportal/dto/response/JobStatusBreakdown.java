package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for job status breakdown statistics
 * Used in dashboard statistics to show count of jobs by status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusBreakdown {
    private Long draft;
    private Long published;
    private Long closed;
}
