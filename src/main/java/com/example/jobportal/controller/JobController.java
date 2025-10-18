package com.example.jobportal.controller;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.*;
import com.example.jobportal.entity.Job;
import com.example.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/jobs")
public class JobController extends BaseController {
    private final JobService jobService;

    @GetMapping("/base")
    public ResponseEntity<ApiResponse<List<JobBaseResponse>>> getBaseJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size

    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobBaseResponse> jobPage = jobService.getBaseJobs(keyword, category, location, pageable);

        PageInfo pageInfo = PageInfo.of(
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements()
        );

        return ok("Get job list successfully", jobPage.getContent(), pageInfo);
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<JobBaseResponseV2>>> getAllJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JobBaseResponseV2> jobPage = jobService.getJobs(keyword, category, location, pageable);
        PageInfo pageInfo = PageInfo.of(jobPage.getNumber(), jobPage.getSize(), jobPage.getTotalElements());
        return ok("Get all jobs successfully", jobPage.getContent(), pageInfo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDetailResponse>> getJob(@PathVariable Long id) {
        JobDetailResponse jobDetailResponse = jobService.getJobDetail(id);
        return ok("", jobDetailResponse);
    }

    @GetMapping("/hr/{id}")
    public ResponseEntity<ApiResponse<List<JobBaseResponse>>> getHrJobs(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size

    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JobBaseResponse> jobBaseResponses = jobService.getJobsByHr(keyword, category, location, status, id, pageable);
        PageInfo pageInfo = PageInfo.of(
                jobBaseResponses.getNumber(),
                jobBaseResponses.getSize(),
                jobBaseResponses.getTotalElements()
        );
        return ok("Get job from hr list successfully", jobBaseResponses.getContent(), pageInfo);
    }

    @GetMapping("/company/{id}")
    public ResponseEntity<ApiResponse<List<JobBaseResponse>>> getCompanyJobs(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size

    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JobBaseResponse> jobBaseResponses = jobService.getJobsByCompany(keyword, category, location, status, id, pageable);
        PageInfo pageInfo = PageInfo.of(
                jobBaseResponses.getNumber(),
                jobBaseResponses.getSize(),
                jobBaseResponses.getTotalElements()
        );
        return ok("Get job from company list successfully", jobBaseResponses.getContent(), pageInfo);
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<JobDetailResponse>> createJob(@RequestBody JobRequest jobRequest) {
        JobDetailResponse jobResponse = JobDetailResponse.fromEntity(jobService.createJob(jobRequest));
        return created("Job created successfully", jobResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDetailResponse>> updateJob(
            @RequestBody JobRequest jobRequest,
            @PathVariable Long id) {
        JobDetailResponse updatedJob = JobDetailResponse.fromEntity(jobService.updateJob(id, jobRequest));
        return ok("Job updated successfully", updatedJob);
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<ApiResponse<Void>> updateJobStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        jobService.changeStatusJob(id, status);
        return ok("Job status updated successfully");
    }


}
