package com.example.jobportal.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.request.SkillRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.JobDetailResponse;
import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.dto.response.PageInfo;
import com.example.jobportal.model.entity.Skill;
import com.example.jobportal.security.CustomUserDetails;
import com.example.jobportal.service.JobService;
import com.example.jobportal.utils.SortUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/jobs")
public class JobController extends BaseController {

    private final JobService jobService;


    private Pageable pageable(int page, int size, String sort, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, SortUtils.jobSort(sort, direction));
    }


    @GetMapping("/base")
    public ResponseEntity<ApiResponse<List<JobBaseResponse>>> getBaseJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "created_at") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<JobBaseResponse> jobPage = jobService.getBaseJobs(
                keyword, category, location, pageable(page, size, sort, direction));
        return ok("Get job list successfully", jobPage.getContent(),
                PageInfo.of(jobPage.getNumber(), jobPage.getSize(), jobPage.getTotalElements()));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getAllJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "created_at") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<JobResponse> jobPage = jobService.getJobs(
                keyword, category, location, pageable(page, size, sort, direction));
        return ok("Get all jobs successfully", jobPage.getContent(),
                PageInfo.of(jobPage.getNumber(), jobPage.getSize(), jobPage.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDetailResponse>> getJobDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        return ok("Get job detail successfully", jobService.getJobDetail(id, userId));
    }

    @GetMapping("/hr/{id}")
    public ResponseEntity<ApiResponse<List<JobBaseResponse>>> getHrJobs(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "created_at") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<JobBaseResponse> jobPage = jobService.getJobsByHr(
                keyword, category, location, status, id, pageable(page, size, sort, direction));
        return ok("Get job from hr list successfully", jobPage.getContent(),
                PageInfo.of(jobPage.getNumber(), jobPage.getSize(), jobPage.getTotalElements()));
    }

    @GetMapping("/company/{id}")
    public ResponseEntity<ApiResponse<List<JobBaseResponse>>> getCompanyJobs(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "created_at") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<JobBaseResponse> jobPage = jobService.getJobsByCompany(
                keyword, category, location, status, id, pageable(page, size, sort, direction));
        return ok("Get job from company list successfully", jobPage.getContent(),
                PageInfo.of(jobPage.getNumber(), jobPage.getSize(), jobPage.getTotalElements()));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "created_at") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Page<JobResponse> jobPage = jobService.getJobs(
                keyword, category, location, status, pageable(page, size, sort, direction));
        return ok("Get jobs successfully", jobPage.getContent(),
                PageInfo.of(jobPage.getNumber(), jobPage.getSize(), jobPage.getTotalElements()));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<JobBaseResponse>> createJob(@Valid @RequestBody JobRequest jobRequest) {
        return created("Job created successfully", jobService.createJob(jobRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobBaseResponse>> updateJob(
            @Valid @RequestBody JobRequest jobRequest,
            @PathVariable Long id) {
        return ok("Job updated successfully", jobService.updateJob(id, jobRequest));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateJobStatus(
            @PathVariable Long id, @RequestParam String status) {
        jobService.changeStatusJob(id, status);
        return ok("Job status updated successfully");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> patchJobStatus(
            @PathVariable Long id, @RequestParam String status) {
        jobService.changeStatusJob(id, status);
        return ok("Job status updated successfully");
    }


    @PostMapping("/{jobId}/saved-jobs")
    public ResponseEntity<ApiResponse<JobBaseResponse>> addJobToSavedJobs(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ok("Save job successfully", jobService.addJobToListSavedJob(jobId, userDetails.getId()));
    }

    @GetMapping("/saved-jobs")
    public ResponseEntity<ApiResponse<List<JobBaseResponse>>> getSavedJobs(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ok("Get saved jobs successfully", jobService.getSavedJobs(userDetails.getId()));
    }

    @DeleteMapping("/saved-jobs/{savedJobId}")
    public ResponseEntity<ApiResponse<Void>> removeJobFromSavedJobs(@PathVariable Long savedJobId) {
        jobService.removeJobFromListSavedJob(savedJobId);
        return ok("Remove saved job successfully");
    }


    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Skill>> createSkill(@Valid @RequestBody SkillRequest request) {
        return created("Skill created successfully", jobService.createSkill(request));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Skill>> updateSkill(
            @Valid @RequestBody SkillRequest request, @PathVariable Long id) {
        return ok("Skill updated successfully", jobService.updateSkill(request, id));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        jobService.deleteSkill(id);
        return ok("Skill deleted successfully");
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkills() {
        return ok("Get skills successfully", jobService.getSkills());
    }
}
