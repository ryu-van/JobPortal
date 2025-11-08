package com.example.jobportal.service;

import com.example.jobportal.dto.request.ResumeRequest;
import com.example.jobportal.dto.response.ResumeBaseResponse;
import com.example.jobportal.dto.response.ResumeDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResumeService {
    ResumeBaseResponse createResume(ResumeRequest resumeRequest);
    ResumeBaseResponse updateResume(ResumeRequest resumeRequest,Long idResume);
    void deleteResume(Long idResume);
    ResumeDetailResponse getResumeDetail(Long idResume);
    void changePrimaryResume(Long idResume,Boolean isPrimary);
    void changePublicResume(Long idResume,Boolean isPublic);
    List<ResumeBaseResponse> getAllResumes(Boolean isPublic, Long userId);
}
