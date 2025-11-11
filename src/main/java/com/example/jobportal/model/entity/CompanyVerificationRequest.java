package com.example.jobportal.model.entity;

import com.example.jobportal.converter.CompanyVerificationStatusConverter;
import com.example.jobportal.model.enums.CompanyVerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDateTime;
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

    @Column(columnDefinition = "jsonb")
    private String documents;

    private String requestedRole;

    @Convert(converter = CompanyVerificationStatusConverter.class)
    private CompanyVerificationStatus status = CompanyVerificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private String rejectionReason;

    private LocalDateTime reviewedAt;

    private BaseAddress address;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // HR hoặc người gửi yêu cầu

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // công ty nếu có sẵn trong hệ thống

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy; // admin nào đã duyệt


}