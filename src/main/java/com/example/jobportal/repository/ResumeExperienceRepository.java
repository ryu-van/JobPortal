package com.example.jobportal.repository;

import com.example.jobportal.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, Long> {
}
