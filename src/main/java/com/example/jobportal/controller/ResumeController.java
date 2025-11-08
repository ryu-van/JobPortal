package com.example.jobportal.controller;


import com.example.jobportal.dto.request.ResumeParseRequest;
import com.example.jobportal.dto.request.ResumeRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.ResumeBaseResponse;
import com.example.jobportal.dto.response.ResumeDetailResponse;
import com.example.jobportal.service.AiResumeParserService;
import com.example.jobportal.service.ResumeService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${spring.base-url}/resumes")
@RequiredArgsConstructor
public class ResumeController extends BaseController {

    private final ResumeService resumeService;
    private final AiResumeParserService aiResumeParserService;

    @PostMapping("/parse-ai")
    public ResponseEntity<ApiResponse<String>> parseAiResume(@RequestBody ResumeParseRequest request) {
        String parsed = aiResumeParserService.parseResume(request.getContent());
        return ok("Parsed successfully", parsed);
    }
    @PostMapping
    public ResponseEntity<ApiResponse<ResumeBaseResponse>> createResume(@RequestBody ResumeRequest resumeRequest) {
        ResumeBaseResponse created = resumeService.createResume(resumeRequest);
        return ok("Resume created successfully", created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeBaseResponse>> updateResume(
            @PathVariable Long id, @RequestBody ResumeRequest resumeRequest) {
        ResumeBaseResponse updated = resumeService.updateResume(resumeRequest, id);
        return ok("Resume updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return ok("Resume deleted successfully", null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeDetailResponse>> getResumeById(@PathVariable Long id) {
        ResumeDetailResponse resume = resumeService.getResumeDetail(id);
        return ok("Get resume successfully", resume);
    }

    @PatchMapping("/{id}/primary")
    public ResponseEntity<ApiResponse<Void>> setPrimaryResume(
            @PathVariable Long id, @RequestParam Boolean isPrimary) {
        resumeService.changePrimaryResume(id, isPrimary);
        return ok("Set primary resume successfully", null);
    }

    @PatchMapping("/{id}/public")
    public ResponseEntity<ApiResponse<Void>> setPublicResume(
            @PathVariable Long id, @RequestParam Boolean isPublic) {
        resumeService.changePublicResume(id, isPublic);
        return ok("Set public resume successfully", null);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeBaseResponse>>> getAllResumes(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isPublic) {
        List<ResumeBaseResponse> resumes = resumeService.getAllResumes(isPublic, userId);
        return ok("Get resume list successfully", resumes);
    }
}

