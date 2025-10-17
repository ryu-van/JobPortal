package com.example.jobportal.controller;

import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.dto.response.PageInfo;
import com.example.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(defaultValue="16") int size

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



}
