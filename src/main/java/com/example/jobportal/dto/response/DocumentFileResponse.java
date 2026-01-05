package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFileResponse {
    private String fileName;
    private String url;
    private String publicId;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
