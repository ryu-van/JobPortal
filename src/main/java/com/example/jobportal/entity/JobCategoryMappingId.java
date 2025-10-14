package com.example.jobportal.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class JobCategoryMappingId implements Serializable {
    private Long jobId;
    private Long categoryId;
}
