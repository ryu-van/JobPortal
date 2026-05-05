package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for period-based statistics
 * Used in dashboard statistics to show counts for specific time periods
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodStats {
    private Long count;
    private String period;
}
