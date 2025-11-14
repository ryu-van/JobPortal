package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.CompanyInvitation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {

    private Long id;
    private String code;
    private String role;
    private String email;
    private Integer maxUses;
    private Integer usedCount;
    private Boolean isActive;
    private Boolean isExpired;
    private Boolean canBeUsed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private CompanyBasicInfo company;
    private UserBasicInfo createdBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyBasicInfo {
        private Long id;
        private String name;
        private String logoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBasicInfo {
        private Long id;
        private String fullName;
        private String email;
    }

    public static InvitationResponse fromEntity(CompanyInvitation entity) {
        if (entity == null) {
            return null;
        }

        return InvitationResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .role(entity.getRole())
                .email(entity.getEmail())
                .maxUses(entity.getMaxUses())
                .usedCount(entity.getUsedCount())
                .isActive(entity.getIsActive())
                .isExpired(entity.isExpired())
                .canBeUsed(entity.canBeUsed())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .company(entity.getCompany() != null ? CompanyBasicInfo.builder()
                        .id(entity.getCompany().getId())
                        .name(entity.getCompany().getName())
                        .logoUrl(entity.getCompany().getLogoUrl())
                        .build() : null)
                .createdBy(entity.getCreatedBy() != null ? UserBasicInfo.builder()
                        .id(entity.getCreatedBy().getId())
                        .fullName(entity.getCreatedBy().getFullName())
                        .email(entity.getCreatedBy().getEmail())
                        .build() : null)
                .build();
    }
}
