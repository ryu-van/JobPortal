package com.example.jobportal.model.enums;

import java.util.List;

public enum UploadType {
    IMAGES(
            "images",
            List.of("image/jpeg", "image/png", "image/webp"),
            5 * 1024 * 1024
    ),
    DOCUMENTS(
            "documents",
            List.of(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ),
            10 * 1024 * 1024
    );
    private final String folder;
    private final List<String> contentTypes;
    private final long maxSize;

    UploadType(String folder, List<String> contentTypes, long maxSize) {
        this.folder = folder;
        this.contentTypes = contentTypes;
        this.maxSize = maxSize;
    }

    public String folder() {
        return folder;
    }

    public List<String> contentTypes() {
        return contentTypes;
    }

    public long maxSize() {
        return maxSize;
    }
}
