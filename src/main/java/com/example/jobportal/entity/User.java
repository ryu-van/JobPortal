package com.example.jobportal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

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
    private String fullName;

    private LocalDate dateOfBirth;

    @NotNull(message = "Code cannot be null")
    private String code;

    private Boolean gender;

    private String phoneNumber;

    @NotNull(message = "Email cannot be null")
    private String email;

    @NotNull(message = "Password cannot be null")
    private String passwordHash;

    private String avatarUrl;

    @Embedded
    private BaseAddress address;

    private Boolean isActive;

    private Boolean isEmailVerified;

    private LocalDateTime lastLoginAt;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;


}
