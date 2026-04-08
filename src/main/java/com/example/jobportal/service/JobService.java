package com.example.jobportal.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.request.SkillRequest;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.model.entity.Skill;

public interface JobService {
    // feature for customer
    Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable);
    Page<JobResponse> getJobs(String keyword, String category, String location, Pageable pageable);
    JobDetailResponse getJobDetail(Long jobId, Long userId);
    // feature for hr,admin company, admin system
    JobBaseResponse createJob(JobRequest jobRequest);
    JobBaseResponse updateJob(Long jobId, JobRequest jobRequest);
    void changeStatusJob(Long jobId,String status);
    Page<JobBaseResponse> getJobsByHr(String keyword, String category, String location,String status,Long hrId, Pageable pageable);
    Page<JobBaseResponse> getJobsByCompany(String keyword, String category, String location,String status,Long companyId, Pageable pageable);
    Page<JobResponse> getJobs(String keyword, String category,String location, String status,Pageable pageable);
    JobBaseResponse addJobToListSavedJob(Long jobId, Long userId);
    void removeJobFromListSavedJob(Long savedJobId);
    List<JobBaseResponse> getSavedJobs(Long userId);
    Skill createSkill(SkillRequest skillRequest);
    Skill updateSkill(SkillRequest skillRequest,Long id);
    void deleteSkill(Long skillId);
    List<Skill> getSkills();


}
