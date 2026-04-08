package com.example.jobportal.repository;

import com.example.jobportal.model.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserId(Long userId);
}
