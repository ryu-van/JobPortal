package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resume_experiences")
public class ResumeExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private String position;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isCurrent = false;

    private Integer displayOrder;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;
}
