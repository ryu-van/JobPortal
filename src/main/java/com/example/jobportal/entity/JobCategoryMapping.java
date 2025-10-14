package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_category_mapping")
public class JobCategoryMapping {

    @EmbeddedId
    private JobCategoryMappingId id = new JobCategoryMappingId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private JobCategory category;
}
