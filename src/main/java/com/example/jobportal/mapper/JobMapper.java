package com.example.jobportal.mapper;

import com.example.jobportal.dto.response.JobBaseResponse;
import com.example.jobportal.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {
    public JobBaseResponse toJobBaseDto(Job job){
        return new JobBaseResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany().getName(),
                job.getAddress().getAddress(),
                job.getCompany().getLogoUrl(),
                job.getIsSalaryNegotiable(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency()
        );
    }
}
