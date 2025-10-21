package com.example.jobportal.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_invitations")
public class CompanyInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // công ty mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // --- Invitation Details ---
    @Column(nullable = false, unique = true, length = 50)
    private String code; // mã mời, ví dụ: ABC-HR-2024-XYZ

    @Column(nullable = false, length = 50)
    private String role = "hr"; // role được mời (thường là 'hr')

    private String email; // email người được mời (optional)

    @Column(name = "max_uses")
    private Integer maxUses = 1; // số lần dùng tối đa (1 = chỉ 1 HR dùng được)

    @Column(name = "used_count")
    private Integer usedCount = 0; // số lần đã dùng

    @Column(nullable = false)
    private LocalDateTime expiresAt; // thời điểm hết hạn mã mời

    private Boolean isActive = true; // còn hoạt động hay không

    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Lifecycle hook ---
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (usedCount == null) usedCount = 0;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean canBeUsed() {
        return isActive && !isExpired() && usedCount < maxUses;
    }
}
