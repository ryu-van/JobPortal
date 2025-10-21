package com.example.jobportal.repository;

import com.example.jobportal.model.entity.CompanyInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyInvitationRepository extends JpaRepository<CompanyInvitation, Long> {
}
