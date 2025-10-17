package com.example.jobportal.dto.response;

import com.example.jobportal.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class JobDetailResponse extends JobBaseResponse {

    private String description;
    private String requirements;
    private String responsibilities;
    private String benefits;
    private String workType;
    private String employmentType;
    private String experienceLevel;
    private Integer numberOfPositions;
    private LocalDateTime applicationDeadline;
    private String status;
    private Boolean isFeatured;
    private Integer viewsCount;
    private Integer applicationsCount;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
    private Set<String> categories;

    public static JobDetailResponse fromEntity(Job job) {
        if (job == null) return null;

        return JobDetailResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany().getName())
                .companyLogo(job.getCompany().getLogoUrl())
                .address(job.getAddress() != null ? job.getAddress().getAddress() : null)
                .isSalaryNegotiable(job.getIsSalaryNegotiable())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())

                .description(job.getDescription())
                .requirements(job.getRequirements())
                .responsibilities(job.getResponsibilities())
                .benefits(job.getBenefits())
                .workType(job.getWorkType())
                .employmentType(job.getEmploymentType())
                .experienceLevel(job.getExperienceLevel())
                .numberOfPositions(job.getNumberOfPositions())
                .applicationDeadline(job.getApplicationDeadline())
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .isFeatured(job.getIsFeatured())
                .viewsCount(job.getViewsCount())
                .applicationsCount(job.getApplicationsCount())
                .publishedAt(job.getPublishedAt())
                .closedAt(job.getClosedAt())
                .categories(job.getCategories() != null
                        ? job.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet())
                        : null)
                .build();
    }
}
