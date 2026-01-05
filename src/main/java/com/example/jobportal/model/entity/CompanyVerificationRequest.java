package com.example.jobportal.model.entity;

import com.example.jobportal.converter.CompanyVerificationStatusConverter;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.Type;

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

    @NotBlank(message = "Giấy phép kinh doanh không được để trống")
    private String businessLicense;

    @NotBlank(message = "Mã số thuế không được để trống")
    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^[0-9]{10}(-[0-9]{3})?$",
            message = "Mã số thuế không hợp lệ")
    private String taxCode;

    @Column(unique = true)
    @NotNull(message = "Company name cannot be null")
    private String companyName;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    @Type(JsonBinaryType.class)
    @Column(name = "document_files", columnDefinition = "jsonb")
    @Builder.Default
    private List<DocumentFile> documentFiles = new ArrayList<>();

    @Convert(converter = CompanyVerificationStatusConverter.class)
    private CompanyVerificationStatus status = CompanyVerificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private String rejectionReason;

    private LocalDateTime reviewedAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "verification_address_id")
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentFile {
        private String fileName;
        private String url;
        private String publicId;
        private String contentType;
        private Long fileSize;
        private LocalDateTime uploadedAt;
    }

    public void addDocumentFile(DocumentFile documentFile) {
        if (this.documentFiles == null) {
            this.documentFiles = new ArrayList<>();
        }
        this.documentFiles.add(documentFile);
    }

    public void removeDocumentFile(String publicId) {
        if (this.documentFiles != null) {
            this.documentFiles.removeIf(doc -> doc.getPublicId().equals(publicId));
        }
    }

    public void clearDocumentFiles() {
        if (this.documentFiles != null) {
            this.documentFiles.clear();
        }
    }

    @Transient
    public List<String> getDocumentUrls() {
        return documentFiles != null ?
                documentFiles.stream().map(DocumentFile::getUrl).toList() :
                new ArrayList<>();
    }
}