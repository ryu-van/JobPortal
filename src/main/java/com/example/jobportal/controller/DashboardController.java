package com.example.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.DashboardStatsResponse;
import com.example.jobportal.service.DashboardService;
import com.example.jobportal.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/dashboard")
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('HR', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = dashboardService.getStats(SecurityUtils.currentUserId());
        return ok("Get dashboard stats successfully", stats);
    }
}
