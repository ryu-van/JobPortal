package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Job;
import com.example.jobportal.model.entity.JobCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JobBaseResponseV2 extends JobBaseResponse {

    private String workType;
    private String employmentType;
    private String experienceLevel;
    private String status;
    private Integer numberOfPositions;
    private Integer applicationsCount;
    private LocalDateTime applicationDeadline;
    private LocalDateTime publishedAt;
    private List<String> categoryNames;

    public static JobBaseResponseV2 fromEntity(Job job) {
        if (job == null) return null;

        return JobBaseResponseV2.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany().getName())
                .address(AddressResponse.fromEntity(job.getAddress()))
                .companyLogo(job.getCompany().getLogoUrl())
                .isSalaryNegotiable(job.getIsSalaryNegotiable())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())

                // ===== v2 fields =====
                .workType(String.valueOf(job.getWorkType()))
                .employmentType(String.valueOf(job.getEmploymentType()))
                .experienceLevel(String.valueOf(job.getExperienceLevel()))
                .status(job.getStatus() != null ? job.getStatus().getValue() : null)
                .numberOfPositions(job.getNumberOfPositions())
                .applicationsCount(job.getApplicationsCount())
                .applicationDeadline(job.getApplicationDeadline())
                .publishedAt(job.getPublishedAt())
                .categoryNames(
                        job.getCategories() != null
                                ? job.getCategories().stream()
                                .map(JobCategory::getName)
                                .toList()
                                : List.of()
                )
                .build();
    }
}
