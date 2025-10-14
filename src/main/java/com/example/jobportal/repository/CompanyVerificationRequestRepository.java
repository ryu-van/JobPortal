package com.example.jobportal.repository;

import com.example.jobportal.entity.CompanyVerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyVerificationRequestRepository extends JpaRepository<CompanyVerificationRequest, Long> {
}
