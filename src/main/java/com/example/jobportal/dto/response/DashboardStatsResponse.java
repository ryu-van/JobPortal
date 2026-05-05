package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for dashboard statistics response
 * Contains role-specific statistics for Admin, HR, and Company Admin users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    // Existing fields - Common for HR and COMPANY_ADMIN
    private long activeJobPostings;
    private long pendingApplications;
    private long totalResumesReceived;
    private Long activeHrUsers; // null for HR role, populated for COMPANY_ADMIN
    
    // New common fields (for all roles)
    private Long totalApplications; // total application count
    private Long totalJobViews; // sum of job views
    private ApplicationStatusBreakdown applicationsByStatus; // status breakdown
    private List<TopJobDto> topJobs; // top 5 jobs by application count
    
    // HR and COMPANY_ADMIN fields
    private Double avgTimeToFirstApplication; // average time to first application
    
    // COMPANY_ADMIN only fields
    private List<TopHrDto> topHrUsers; // top 5 HR users
    private JobStatusBreakdown jobsByStatus; // job status breakdown
    private Double avgApplicationsPerJob; // average applications per job
    
    // ADMIN only fields
    private Long totalUsers;
    private Long totalCompanies;
    private Long verifiedCompanies;
    private Long totalJobs;
    private PeriodStats newUsersInPeriod;
    private PeriodStats newCompaniesInPeriod;
    private PeriodStats newJobsInPeriod;
    private PeriodStats newApplicationsInPeriod;
}
