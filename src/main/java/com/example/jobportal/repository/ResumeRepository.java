package com.example.jobportal.repository;

import com.example.jobportal.dto.response.ResumeBaseResponse;
import com.example.jobportal.model.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Query("""
       SELECT new com.example.jobportal.dto.response.ResumeBaseResponse(
           r.id,
           r.title,
           r.isPrimary,
           r.isPublic,
           r.createdAt
       )
       FROM Resume r
       WHERE (:userId IS NULL OR r.user.id = :userId)
         AND (:isPublic IS NULL OR r.isPublic = :isPublic)
       ORDER BY r.createdAt DESC
       """)
    List<ResumeBaseResponse> getAllResumes(Boolean isPublic, Long userId);

}
