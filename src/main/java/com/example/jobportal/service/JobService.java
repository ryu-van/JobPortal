package com.example.jobportal.service;

import com.example.jobportal.dto.response.JobBaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface JobService {
    Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable);

}
