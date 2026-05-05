package com.example.jobportal.service;

import com.example.jobportal.constant.AppConstants;
import com.example.jobportal.dto.response.DashboardStatsResponse;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.ApplicationStatus;
import com.example.jobportal.model.enums.JobStatus;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(Long userId) {
        // Resolve the caller's company
        User caller = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound("User not found with id: " + userId));

        if (caller.getCompany() == null) {
            throw new AccessDeniedException("Access denied: user has no associated company");
        }

        Long companyId = caller.getCompany().getId();

        // Count active job postings (status = published)
        long activeJobPostings = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.PUBLISHED);

        // Count pending applications for the company's jobs
        long pendingApplications = applicationRepository.countByCompanyIdAndStatus(companyId, ApplicationStatus.PENDING);

        // Count total resumes (all applications) for the company's jobs
        long totalResumesReceived = applicationRepository.countByCompanyId(companyId);

        // Count active HR users only for COMPANY_ADMIN callers
        Long activeHrUsers = null;
        if (isCallerCompanyAdmin()) {
            activeHrUsers = userRepository.countActiveHrUsersByCompanyId(companyId);
        }

        log.debug("Dashboard stats for company {}: jobs={}, pendingApps={}, totalResumes={}, activeHr={}",
                companyId, activeJobPostings, pendingApplications, totalResumesReceived, activeHrUsers);

        return DashboardStatsResponse.builder()
                .activeJobPostings(activeJobPostings)
                .pendingApplications(pendingApplications)
                .totalResumesReceived(totalResumesReceived)
                .activeHrUsers(activeHrUsers)
                .build();
    }

    /**
     * Returns {@code true} when the currently authenticated principal holds the
     * {@code ROLE_COMPANY_ADMIN} authority.
     */
    private boolean isCallerCompanyAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + AppConstants.ROLE_COMPANY_ADMIN));
    }
}
