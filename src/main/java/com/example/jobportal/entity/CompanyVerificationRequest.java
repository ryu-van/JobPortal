package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_verification_requests")
public class CompanyVerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private String businessLicense;

    private String taxCode;

    private String contactPerson;

    private String contactEmail;

    private String contactPhone;

    @Column(columnDefinition = "jsonb")
    private String documents; // JSONB trong PostgreSQL: chứa link file giấy phép, ủy quyền...

    private String requestedRole; // 'hr' hoặc 'company_admin'

    private String status = "pending"; // 'pending', 'approved', 'rejected'

    @Column(columnDefinition = "TEXT")
    private String adminNotes; // ghi chú của admin

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // HR hoặc người gửi yêu cầu

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // công ty nếu có sẵn trong hệ thống

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy; // admin nào đã duyệt

    // --- Lifecycle hooks ---
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
