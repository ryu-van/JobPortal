package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Job;
import com.example.jobportal.model.entity.JobCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class JobBaseResponseV2 extends JobBaseResponse {
    private String workType;
    private String employmentType;
    private String experienceLevel;
    private Integer numberOfPositions;
    private LocalDateTime applicationDeadline;
    private List<String> categoryNames;
    private String country; // Thêm trường country

    public JobBaseResponseV2() {
        super();
    }

    public JobBaseResponseV2(
            Long id,
            String title,
            String companyName,
            String street,
            String ward,
            String district,
            String city,
            String companyLogo,
            Boolean isSalaryNegotiable,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String salaryCurrency,
            String workType,
            String employmentType,
            String experienceLevel,
            Integer numberOfPositions,
            LocalDateTime applicationDeadline,
            String categoryNames
    ) {
        super(id, title, companyName, street, ward, district, city, companyLogo,
                isSalaryNegotiable, salaryMin, salaryMax, salaryCurrency);

        this.workType = workType;
        this.employmentType = employmentType;
        this.experienceLevel = experienceLevel;
        this.numberOfPositions = numberOfPositions;
        this.applicationDeadline = applicationDeadline;
        this.country = country;

        if (categoryNames != null && !categoryNames.trim().isEmpty()) {
            this.categoryNames = Arrays.asList(categoryNames.split(",\\s*"));
        } else {
            this.categoryNames = List.of();
        }
    }

    // Method fromEntity giữ nguyên
    public static JobBaseResponseV2 fromEntity(Job job) {
        if (job == null) return null;

        return JobBaseResponseV2.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany().getName())
                .companyLogo(job.getCompany().getLogoUrl())
                .street(job.getAddress().getStreet())
                .ward(job.getAddress().getWard())
                .district(job.getAddress().getDistrict())
                .city(job.getAddress().getCity())
                .country(job.getAddress().getCountry())
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
                                        .toList() : List.of()
                )
                .build();
    }
}