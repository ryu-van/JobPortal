package com.example.jobportal.dto.response;

import com.example.jobportal.model.enums.UploadType;
import lombok.*;

@Builder
@Setter
@NoArgsConstructor
@Getter
@Data
@AllArgsConstructor
public class UploadResultResponse {
    private String originalFilename;
    private String url;
    private String publicId;
    private UploadType uploadType;
    private String status;
    private String error;
}
