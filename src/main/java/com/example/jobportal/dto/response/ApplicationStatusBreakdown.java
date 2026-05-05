package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for application status breakdown statistics
 * Used in dashboard statistics to show count of applications by status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusBreakdown {
    private Long pending;
    private Long reviewing;
    private Long accepted;
    private Long rejected;
}
