package com.example.jobportal.service;

import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    @Override
    public Page<JobBaseResponse> getBaseJobs(String keyword, String category, String location, Pageable pageable) {
        return jobRepository.getBaseJobs(keyword, category, location, pageable) ;
    }
}
