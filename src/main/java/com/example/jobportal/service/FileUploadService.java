package com.example.jobportal.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileUploadService {
    String uploadFile(MultipartFile file);
    void deleteFile(String publicId);
}
