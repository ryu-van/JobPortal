package com.example.jobportal.service;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface JobService {
    Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable);
    Job createJob(JobRequest jobRequest);
    Job updateJob(Long jobId, JobRequest jobRequest);
    void changeStatusJob(Long jobId,String status);
    JobDetailResponse getJobDetail(Long jobId);
    Page<JobBaseResponse> getJobsByHr(Long hrId, Pageable pageable);
    Page<JobBaseResponse> getJobsByCompany(Long companyId, Pageable pageable);



}
