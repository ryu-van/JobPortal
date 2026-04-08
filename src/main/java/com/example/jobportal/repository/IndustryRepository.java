package com.example.jobportal.repository;

import com.example.jobportal.model.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {
    Optional<Industry> findByName(String name);

    List<Industry> findByNameContainingIgnoreCase(String industryName);

    boolean existsByCode(String code);

}
