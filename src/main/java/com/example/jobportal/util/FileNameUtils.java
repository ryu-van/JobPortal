package com.example.jobportal.util;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class FileNameUtils {
    public static String generateUniqueFileName(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("File cannot be null or have no name");
        }

        String originalFileName = file.getOriginalFilename();

        String extension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        String baseName = originalFileName.substring(0, dotIndex >= 0 ? dotIndex : originalFileName.length());
        baseName = normalizeFileName(baseName);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8);

        String prefix;
        if (isImageFile(file)) {
            prefix = "image";
        } else if (isDocumentFile(file)) {
            prefix = "resume";
        } else {
            prefix = "file";
        }

        return String.format("%s_%s_%s%s", prefix, timestamp, random, extension);
    }


    public static String normalizeFileName(String filename) {
        if (filename == null) return "file";
        String normalized = Normalizer.normalize(filename, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("\\s+", "_");
        return normalized.replaceAll("[^a-zA-Z0-9_\\-]", "").toLowerCase();
    }


    public static Boolean isImageFile(MultipartFile file) {
        if (file == null || file.getContentType() == null) return false;
        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        return contentType.startsWith("image/");
    }

    public static Boolean isDocumentFile(MultipartFile file) {
        if (file == null || file.getContentType() == null) return false;
        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        return contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
}
