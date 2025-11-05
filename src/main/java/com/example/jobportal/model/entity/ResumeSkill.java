package com.example.jobportal.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resume_skills")
public class ResumeSkill {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String skillName;

    private String proficiencyLevel;

    private Integer yearsOfExperience;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;
}
