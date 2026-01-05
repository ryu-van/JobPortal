package com.example.jobportal.controller;

import com.example.jobportal.dto.request.ApplicationRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.ApplicationResponse;
import com.example.jobportal.dto.response.ApplicationStatusHistoryResponse;
import com.example.jobportal.security.CustomUserDetails;
import com.example.jobportal.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/applications")
public class ApplicationController extends BaseController {

    private final ApplicationService applicationService;
    

    @PostMapping("/")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyForJob(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody ApplicationRequest request
    ) {
        if (currentUser == null) {
            return unauthorized("Vui lòng đăng nhập để ứng tuyển");
        }
        ApplicationResponse response = applicationService.applyForJob(currentUser.getId(), request);
        return created("Ứng tuyển thành công", response);
    }


    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<Void>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam String newStatus,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (currentUser == null) {
            return unauthorized("Vui lòng đăng nhập để cập nhật trạng thái");
        }

        applicationService.updateStatus(applicationId, newStatus, notes, currentUser.getId());
        return ok("Cập nhật trạng thái ứng tuyển thành công");
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (currentUser == null) {
            return unauthorized("Vui lòng đăng nhập để xem danh sách ứng tuyển");
        }
        List<ApplicationResponse> responses = applicationService.getApplicationsByUser(currentUser.getId());
        return ok("Lấy danh sách đơn ứng tuyển thành công", responses);
    }


    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicationsByJob(
            @PathVariable Long jobId
    ) {
        List<ApplicationResponse> responses = applicationService.getApplicationsByJob(jobId);
        return ok("Lấy danh sách ứng viên ứng tuyển theo job thành công", responses);
    }

    @GetMapping("/{applicationId}/history")
    public ResponseEntity<ApiResponse<List<ApplicationStatusHistoryResponse>>> getStatusHistory(
            @PathVariable Long applicationId
    ) {
        List<ApplicationStatusHistoryResponse> responses = applicationService.getStatusHistory(applicationId);
        return ok("Lấy lịch sử trạng thái hồ sơ thành công", responses);
    }
}
