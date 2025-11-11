package com.example.jobportal.repository;

import com.example.jobportal.model.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(Long userId);
    List<Application> findByJobId(Long jobId);
    Optional<Application> findByUserIdAndJobId(Long userId, Long jobId);

    @Query("""
        SELECT COUNT(a) > 0 FROM Application a
        WHERE a.user.id = :userId AND a.job.id = :jobId
    """)
    boolean existsByUserAndJob(Long userId, Long jobId);

}
