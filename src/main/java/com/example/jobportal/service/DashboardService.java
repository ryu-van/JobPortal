package com.example.jobportal.service;

import com.example.jobportal.dto.response.DashboardStatsResponse;

public interface DashboardService {

    /**
     * Returns aggregated dashboard statistics scoped to the company of the given user.
     *
     * <ul>
     *   <li>{@code activeJobPostings} — count of jobs with status {@code published} for the company</li>
     *   <li>{@code pendingApplications} — count of applications with status {@code pending} for the company's jobs</li>
     *   <li>{@code totalResumesReceived} — total count of all applications for the company's jobs</li>
     *   <li>{@code activeHrUsers} — count of active HR users in the company; {@code null} when the caller has the HR role</li>
     * </ul>
     *
     * @param userId the ID of the authenticated caller
     * @return a {@link DashboardStatsResponse} populated according to the caller's role
     * @throws org.springframework.security.access.AccessDeniedException if the user has no associated company
     */
    DashboardStatsResponse getStats(Long userId);
}
