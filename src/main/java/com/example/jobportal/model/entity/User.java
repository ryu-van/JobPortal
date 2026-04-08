package com.example.jobportal.model.entity;

import com.example.jobportal.model.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Full name cannot be null")
    @Column(name = "full_name", columnDefinition = "VARCHAR(255)")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotNull(message = "Code cannot be null")
    @Column(columnDefinition = "VARCHAR(255)")
    private String code;

    @Column(length = 20, columnDefinition = "VARCHAR(50)")
    private Gender gender;

    @Column(name = "phone_number", columnDefinition = "VARCHAR(20)")
    private String phoneNumber;

    @NotNull(message = "Email cannot be null")
    @Column(columnDefinition = "VARCHAR(255)")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Column(name = "password_hash", columnDefinition = "VARCHAR(255)")
    private String passwordHash;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "avatar_public_id", columnDefinition = "VARCHAR(255)")
    private String avatarPublicId;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "verification_token", columnDefinition = "VARCHAR(255)")
    private String verificationToken;

    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    public void setVerificationToken(String token) {
        this.verificationToken = token;
        this.tokenExpiryDate = LocalDateTime.now().plus(24, ChronoUnit.HOURS);
    }

    public boolean isTokenExpired() {
        return tokenExpiryDate != null && tokenExpiryDate.isBefore(LocalDateTime.now());
    }
}