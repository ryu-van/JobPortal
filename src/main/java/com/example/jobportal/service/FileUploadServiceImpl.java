package com.example.jobportal.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.jobportal.dto.response.UploadResultResponse;
import com.example.jobportal.model.enums.UploadType;
import com.example.jobportal.utils.FileNameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
    private final Cloudinary cloudinary;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    public UploadResultResponse uploadSingle(MultipartFile file, UploadType type) {
        try {
            validateFile(file, type);
            String fileName = FileNameUtils.generateUniqueFileName(file);

            // Use "raw" for documents (PDF, DOC, DOCX) so Cloudinary serves them correctly
            String resourceType = (type == UploadType.DOCUMENTS) ? "raw" : "image";

            var result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "JobPortal/" + type.getFolder(),
                            "public_id", fileName,
                            "resource_type", resourceType,
                            "type", "upload",
                            "access_mode", "public",
                            "overwrite", true
                    ));
            return new UploadResultResponse(
                    file.getOriginalFilename(),
                    result.get("secure_url").toString(),
                    result.get("public_id").toString(),
                    type,
                    "SUCCESS",
                    null
            );
        } catch (IOException e) {
            log.error("Upload failed: {}", file.getOriginalFilename(), e);
            return new UploadResultResponse(
                    file.getOriginalFilename(),
                    null,
                    null,
                    type,
                    "FAILED",
                    e.getMessage()
            );
        }
    }

    @Override
    public List<UploadResultResponse> uploadFiles(List<MultipartFile> files, UploadType type) {
        return files.stream()
                .map(file -> CompletableFuture.supplyAsync(
                        () -> uploadSingle(file, type), executor
                ))
                .map(CompletableFuture::join)
                .toList();
    }

    @Override
    public UploadResultResponse replaceFile(MultipartFile newFile, String oldPublicId, UploadType type) {
        if (oldPublicId != null && !oldPublicId.isEmpty()) {
            deleteFile(oldPublicId);
        }
        return uploadSingle(newFile, type);
    }

    private void validateFile(MultipartFile file, UploadType type) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File rỗng");

        if (file.getSize() > type.getMaxSize())
            throw new IllegalArgumentException("File vượt quá dung lượng cho phép: " + type.getMaxSize() / (1024 * 1024) + "MB");

        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty() || "application/octet-stream".equalsIgnoreCase(contentType)) {
            contentType = detectContentTypeFromExtension(file.getOriginalFilename());
        }

        String finalContentType = contentType.toLowerCase();
        boolean isSupported = type.getContentTypes().stream()
                .anyMatch(supportedType -> supportedType.equalsIgnoreCase(finalContentType));

        if (!isSupported) {
            log.warn("Unsupported file format: {} for type: {}", finalContentType, type);
            throw new IllegalArgumentException("Không hỗ trợ định dạng file: " + finalContentType);
        }
    }

    private String detectContentTypeFromExtension(String filename) {
        if (filename == null) return "application/octet-stream";
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("Public ID is null or empty, cannot delete file.");
            return;
        }
        try {
            // Try raw first (documents), fall back to image
            var result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "raw"));
            if ("not found".equals(result.get("result"))) {
                cloudinary.uploader().destroy(publicId,
                        ObjectUtils.asMap("resource_type", "image"));
            }
            log.info("Deleted Cloudinary file with public_id: {}", publicId);
        } catch (IOException e) {
            log.error("Error deleting Cloudinary file: {}", e.getMessage());
        }
    }

    @Override
    public void deleteFiles(List<String> publicIds) {
        publicIds.stream().filter(publicId -> !publicId.isEmpty())
                .forEach(this::deleteFile);

    }
}
