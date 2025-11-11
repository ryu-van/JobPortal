package com.example.jobportal.service;

import com.example.jobportal.dto.request.ApplicationRequest;
import com.example.jobportal.dto.response.ApplicationResponse;
import com.example.jobportal.dto.response.ApplicationStatusHistoryResponse;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse applyForJob(Long userId, ApplicationRequest applicationRequest);
    void updateStatus(Long applicationId, String newStatus, String notes, Long reviewerId);
    List<ApplicationResponse> getApplicationsByUser(Long userId);
    List<ApplicationResponse> getApplicationsByJob(Long jobId);
    List<ApplicationStatusHistoryResponse> getStatusHistory(Long applicationId);


}
