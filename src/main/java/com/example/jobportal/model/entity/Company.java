package com.example.jobportal.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(unique = true, columnDefinition = "VARCHAR(255)")
    @NotNull(message = "Company name cannot be null")
    private String name;

    @Column(unique = true, columnDefinition = "VARCHAR(255)")
    @NotNull(message = "Company email cannot be null")
    private String email;

    @Column(name = "phone_number", columnDefinition = "VARCHAR(20)")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "tax_code", columnDefinition = "VARCHAR(50)")
    private String taxCode;

    private LocalDateTime establishmentDate;

    @Column(name = "company_size", columnDefinition = "VARCHAR(50)")
    private String companySize;

    @Column(columnDefinition = "VARCHAR(255)")
    private String website;

    @Column(name = "logo_url", columnDefinition = "VARCHAR(255)")
    private String logoUrl;

    @Column(name = "logo_public_id", columnDefinition = "VARCHAR(255)")
    private String logoPublicId;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Address> addresses = new HashSet<>();

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "industry_id")
    private Industry industry;

    public void addAddress(Address address) {
        if (addresses == null) {
            addresses = new HashSet<>();
        }
        addresses.add(address);
        address.setCompany(this);
    }

}