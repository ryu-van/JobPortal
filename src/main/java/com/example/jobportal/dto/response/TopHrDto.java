package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for top HR user statistics
 * Used in dashboard statistics to show HR users with most jobs created
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopHrDto {
    private Long userId;
    private String fullName;
    private Long jobsCreated;
}
