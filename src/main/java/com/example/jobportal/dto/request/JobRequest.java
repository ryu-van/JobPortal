package com.example.jobportal.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(min = 50, message = "Description must be at least 50 characters")
    private String description;

    @NotBlank(message = "Requirements are required")
    private String requirements;
    @NotBlank(message = "Responsibilities are required")
    private String responsibilities;

    @NotBlank(message = "Benefits are required")
    private String benefits;

    // Address fields
    private String address;
    private String city;
    private String country;

    @Pattern(regexp = "REMOTE|ONSITE|HYBRID", message = "Invalid work type")
    private String workType;

    @Pattern(regexp = "FULL_TIME|PART_TIME|CONTRACT|INTERNSHIP|FREELANCE", message = "Invalid employment type")
    private String employmentType;

    @Pattern(regexp = "ENTRY|JUNIOR|MIDDLE|SENIOR|LEAD|MANAGER", message = "Invalid experience level")
    private String experienceLevel;

    private Boolean isSalaryNegotiable = false;

    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum salary must be positive")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum salary must be positive")
    private BigDecimal salaryMax;

    @Pattern(regexp = "VND|USD|EUR", message = "Invalid currency")
    private String salaryCurrency = "VND";

    private String skills;

    @Min(value = 1, message = "Must have at least 1 position")
    @Max(value = 100, message = "Cannot exceed 100 positions")
    private Integer numberOfPositions = 1;

    @Future(message = "Application deadline must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime applicationDeadline;

    @Pattern(regexp = "DRAFT|PUBLISHED|CLOSED|ARCHIVED", message = "Invalid status")
    private String status = "DRAFT";


    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotEmpty(message = "At least one category is required")
    private Set<Long> categoryIds; // Support multiple categories

    @AssertTrue(message = "Salary max must be greater than salary min")
    public boolean isSalaryRangeValid() {
        if (salaryMin != null && salaryMax != null) {
            return salaryMax.compareTo(salaryMin) >= 0;
        }
        return true;
    }
}