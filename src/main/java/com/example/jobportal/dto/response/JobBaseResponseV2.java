package com.example.jobportal.dto.response;

import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.JobCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class JobBaseResponseV2 extends JobBaseResponse{
    private String workType;
    private String employmentType;
    private String experienceLevel;
    private Integer numberOfPositions;
    private LocalDateTime applicationDeadline;
    private List<String> categoryNames;

    private JobBaseResponseV2 fromEntity(Job job){
        if (job == null) return null;

        return JobBaseResponseV2.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany().getName())
                .companyLogo(job.getCompany().getLogoUrl())
                .address(job.getAddress() != null ? job.getAddress().getAddress() : null)
                .isSalaryNegotiable(job.getIsSalaryNegotiable())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())

                .workType(job.getWorkType())
                .employmentType(job.getEmploymentType())
                .experienceLevel(job.getExperienceLevel())
                .numberOfPositions(job.getNumberOfPositions())
                .applicationDeadline(job.getApplicationDeadline())
                .categoryNames(
                        job.getCategories() != null ?
                                job.getCategories().stream()
                                        .map(JobCategory::getName)
                                        .toList() : null
                )
                .build();
    }


}
