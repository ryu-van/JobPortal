package com.example.jobportal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.jobportal.model.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    @EntityGraph(attributePaths = {"company", "address", "categories", "skills"})
    Page<Job> findAll(org.springframework.data.jpa.domain.Specification<Job> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "address", "categories", "skills"})
    java.util.Optional<Job> findWithDetailsById(Long id);
}
