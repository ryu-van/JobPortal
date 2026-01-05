package com.example.jobportal.service;

import com.example.jobportal.dto.response.UploadResultResponse;
import com.example.jobportal.model.enums.UploadType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface FileUploadService {
    UploadResultResponse uploadSingle(MultipartFile file, UploadType type);
    List<UploadResultResponse> uploadFiles(List<MultipartFile> files, UploadType type);
    UploadResultResponse replaceFile(MultipartFile newFile, String oldPublicId, UploadType type);
    void deleteFile(String publicId);
    void deleteFiles(List<String> publicIds);
}
