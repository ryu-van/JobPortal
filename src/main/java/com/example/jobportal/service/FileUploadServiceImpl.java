package com.example.jobportal.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.jobportal.util.FileNameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService{
    private final Cloudinary cloudinary;
    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String uniqueFileName = FileNameUtils.generateUniqueFileName(file);
            var uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", uniqueFileName,
                            "folder", "JobPortal",
                            "resource_type", "auto"
                    ));
            String fileUrl = uploadResult.get("secure_url").toString();
            log.info("Uploaded file to Cloudinary: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted Cloudinary file with public_id: {}", publicId);
        } catch (IOException e) {
            log.error("Error deleting Cloudinary file: {}", e.getMessage());
        }

    }
}
