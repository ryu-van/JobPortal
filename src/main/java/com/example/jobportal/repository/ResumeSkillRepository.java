package com.example.jobportal.repository;

import com.example.jobportal.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Long> {
}
