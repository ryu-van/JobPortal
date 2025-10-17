package com.example.jobportal.service;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobBaseResponseV2;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface JobService {
    // feature for customer
    Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable);
    Page<JobBaseResponseV2> getJobs(String keyword, String category, String location, Pageable pageable);
    JobDetailResponse getJobDetail(Long jobId);
    // feature for hr,admin company, admin system
    Job createJob(JobRequest jobRequest);
    Job updateJob(Long jobId, JobRequest jobRequest);
    void changeStatusJob(Long jobId,String status);
    Page<JobBaseResponse> getJobsByHr(String keyword, String category, String location,String status,Long hrId, Pageable pageable);
    Page<JobBaseResponse> getJobsByCompany(String keyword, String category, String location,String status,Long companyId, Pageable pageable);


}
