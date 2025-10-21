package com.example.jobportal.model.entity;

import com.example.jobportal.converter.JobStatusConverter;
import com.example.jobportal.model.enums.JobStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Job title cannot be null")
    private String title;

    @NotNull(message = "Job description cannot be null")
    private String description;

    private String requirements;
    private String responsibilities;
    private String benefits;

    @Embedded
    private BaseAddress address;

    private String workType;           // nên đặt camelCase
    private String employmentType;
    private String experienceLevel;

    @Column(nullable = false)
    private Boolean isSalaryNegotiable = false;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    private String salaryCurrency = "VND";
    private String skills;
    private Integer numberOfPositions = 1;

    // Có giờ cụ thể
    private LocalDateTime applicationDeadline;

    @Convert(converter = JobStatusConverter.class)
    private JobStatus status;

    private Integer viewsCount = 0;
    private Integer applicationsCount = 0;
    private Boolean isFeatured = false;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_category_mapping",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<JobCategory> categories;


}
