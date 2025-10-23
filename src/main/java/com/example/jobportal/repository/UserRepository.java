package com.example.jobportal.repository;

import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.model.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    UserDetails findByEmail(@NotNull(message = "Email cannot be null") String email);

    @Query("""
                SELECT new com.example.jobportal.dto.response.UserBaseResponse(
                    u.id,
                    u.fullName,
                    u.code,
                    u.email,
                    u.gender,
                    u.role.name,
                    u.isActive
                )
                FROM User u
                WHERE u.role.id = 2
                AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                    OR LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:companyName IS NULL OR LOWER(u.company.name) LIKE LOWER(CONCAT('%', :companyName, '%')))
                AND (:isActive IS NULL OR u.isActive = :isActive)
            """)
    Page<UserBaseResponse> getUserByHrRole(String keyword, String companyName, Boolean isActive, Pageable pageable);

    @Query("""
                SELECT new com.example.jobportal.dto.response.UserBaseResponse(
                    u.id,
                    u.fullName,
                    u.code,
                    u.email,
                    u.gender,
                    u.role.name,
                    u.isActive
                )
                FROM User u
                WHERE u.role.id = :roleId
                AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:isActive IS NULL OR u.isActive = :isActive)
            """)
    Page<UserBaseResponse> getUserByRole(String keyword, Boolean isActive, Pageable pageable, Long roleId);
}
