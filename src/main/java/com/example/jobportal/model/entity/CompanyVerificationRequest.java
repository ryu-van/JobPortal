package com.example.jobportal.model.entity;

import com.example.jobportal.converter.CompanyVerificationStatusConverter;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_verification_requests")
public class CompanyVerificationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã số thuế không được để trống")
    @Column(nullable = false, unique = true)
    @Pattern(
            regexp = "^[0-9]{6}-[0-9]{3}([0-9])?$",
            message = "Mã số thuế phải đúng định dạng XXXXXX-XXX hoặc XXXXXX-XXXX"
    )
    private String taxCode;

    @Column(unique = true)
    @NotNull(message = "Company name cannot be null")
    private String companyName;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    private String website;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    private String logoUrl;

    private String logoPublicId;

    @Column(nullable = false)
    @Convert(converter = CompanyVerificationStatusConverter.class)
    private CompanyVerificationStatus status = CompanyVerificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private String rejectionReason;

    private LocalDateTime reviewedAt;

    private String description;

    private String companySize;

    private LocalDateTime establishmentDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "verification_address_id")
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "verificationRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompanyVerificationDocument> documents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    public void addDocument(CompanyVerificationDocument document) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        document.setVerificationRequest(this);
        this.documents.add(document);
    }

    public void removeDocument(String publicId) {
        if (this.documents != null) {
            this.documents.removeIf(doc -> publicId.equals(doc.getPublicId()));
        }
    }

    public void clearDocuments() {
        if (this.documents != null) {
            this.documents.clear();
        }
    }

    @Transient
    public List<String> getDocumentUrls() {
        return documents != null
                ? documents.stream().map(CompanyVerificationDocument::getFileUrl).toList()
                : new ArrayList<>();
    }
}