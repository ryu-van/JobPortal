package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.ApplicationStatusHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusHistoryResponse {
    private String oldStatus;
    private String newStatus;
    private String notes;
    private LocalDateTime changedAt;
    private String changedBy;

    public static ApplicationStatusHistoryResponse fromEntity(ApplicationStatusHistory history) {
        return ApplicationStatusHistoryResponse.builder()
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .notes(history.getNotes())
                .changedAt(history.getChangedAt())
                .changedBy(history.getChangedBy() != null ? history.getChangedBy().getFullName() : "System")
                .build();
    }
}
