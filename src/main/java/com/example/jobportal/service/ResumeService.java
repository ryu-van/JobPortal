package com.example.jobportal.service;

import com.example.jobportal.dto.request.ResumeRequest;
import com.example.jobportal.dto.response.ResumeBaseResponse;

public interface ResumeService {
    ResumeBaseResponse createResume(ResumeRequest resumeRequest);
    ResumeBaseResponse updateResume(ResumeRequest resumeRequest,Long idResume);
}
