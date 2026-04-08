package com.example.jobportal.model.entity;

import com.example.jobportal.converter.EmploymentTypeConverter;
import com.example.jobportal.converter.ExperienceLevelConverter;
import com.example.jobportal.converter.JobStatusConverter;
import com.example.jobportal.converter.WorkTypeConverter;
import com.example.jobportal.model.enums.EmploymentType;
import com.example.jobportal.model.enums.ExperienceLevel;
import com.example.jobportal.model.enums.JobStatus;
import com.example.jobportal.model.enums.WorkType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    @Convert(converter = WorkTypeConverter.class)
    @Column(name = "work_type")
    private WorkType workType;

    @Convert(converter = EmploymentTypeConverter.class)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Convert(converter = ExperienceLevelConverter.class)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    @Column(nullable = false)
    private Boolean isSalaryNegotiable = false;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    private String salaryCurrency = "VND";
    private Integer numberOfPositions = 1;

    // Có giờ cụ thể
    private LocalDateTime applicationDeadline;

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

    @ManyToMany
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();


}
