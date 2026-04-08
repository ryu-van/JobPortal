package com.example.jobportal.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum UploadType {
    IMAGES(
            "images",
            List.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "image/jpg",
                    "image/gif",
                    "image/bmp",
                    "image/svg+xml",
                    "image/tiff",
                    "image/x-icon",
                    "image/heic",
                    "image/heif"
            ),
            20 * 1024 * 1024
    ),
    DOCUMENTS(
            "documents",
            List.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "image/jpg"
            ),
            30 * 1024 * 1024
    );

    private final String folder;
    private final List<String> contentTypes;
    private final long maxSize;
}
