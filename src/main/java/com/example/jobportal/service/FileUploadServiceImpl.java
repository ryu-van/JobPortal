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
            String publicId = "JobPortal/" + type.folder() + "/" + fileName;
            var result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "JobPortal/" + type.folder(),
                            "public_id", fileName,
                            "resource_type", "auto",
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
        if (file.isEmpty())
            throw new IllegalArgumentException("File rỗng");

        if (file.getSize() > type.maxSize())
            throw new IllegalArgumentException("File vượt quá dung lượng cho phép");

        if (!type.contentTypes().contains(file.getContentType()))
            throw new IllegalArgumentException("Không hỗ trợ định dạng file");
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("Public ID is null or empty, cannot delete file.");
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
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
