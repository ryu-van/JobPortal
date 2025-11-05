package com.example.jobportal.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull(message = "Company name cannot be null")
    private String name;

    @Column(unique = true)
    @NotNull(message = "Company email cannot be null")
    private String email;

    private String phone;

    private String description;

    private String industry;

    private String companySize;

    private String website;

    private String logoUrl;

    @Embedded
    private BaseAddress address;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;




}
