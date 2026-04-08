package com.example.jobportal.repository;

import com.example.jobportal.model.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Set<Skill> findByIdIn(Set<Long> ids);

    Optional<Skill> findById(Long id);
}
