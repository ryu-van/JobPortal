package com.example.jobportal.repository;

import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.model.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("""
                SELECT new com.example.jobportal.dto.response.UserBaseResponse(
                    u.id,
                    u.fullName,
                    u.code,
                    u.email,
                    u.gender,
                    u.role.id,
                    u.role.name,
                    u.isActive,
                    u.isEmailVerified,
                    u.tokenExpiryDate,
                    u.phoneNumber
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
                    u.role.id,
                    u.role.name,
                    u.isActive,
                    u.isEmailVerified,
                    u.tokenExpiryDate,
                    u.phoneNumber
                )
                FROM User u
                WHERE u.role.id = :roleId
                AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:isActive IS NULL OR u.isActive = :isActive)
            """)
    Page<UserBaseResponse> getUserByRole(String keyword, Boolean isActive, Pageable pageable, Long roleId);

    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);

     Optional<User> findById(Long id);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND u.isActive = true")
    List<User> findAllByRoleName(String roleName);

    boolean existsByCode(String code);

    @Query("""
    SELECT new com.example.jobportal.dto.response.UserBaseResponse(
        u.id,
        u.fullName,
        u.code,
        u.email,
        u.gender,
        u.role.id,
        u.role.name,
        u.isActive,
        u.isEmailVerified,
        u.tokenExpiryDate,
        u.phoneNumber
    )
    FROM User u
    WHERE u.company.id = :companyId
      AND (:keyword IS NULL OR 
           LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      AND (:isActive IS NULL OR u.isActive = :isActive)
    ORDER BY 
        CASE WHEN :asc = 'asc' THEN u.fullName END ASC,
        CASE WHEN :asc = 'desc' THEN u.fullName END DESC
""")
    List<UserBaseResponse> getUserInCompany(
            String keyword,
            Boolean isActive,
            Long companyId,
            String asc
    );


}
