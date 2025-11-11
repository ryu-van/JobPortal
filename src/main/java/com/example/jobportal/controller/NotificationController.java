package com.example.jobportal.controller;

import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.NotificationResponse;
import com.example.jobportal.security.CustomUserDetails;
import com.example.jobportal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/notifications")
public class NotificationController extends BaseController {

    private final NotificationService notificationService;


    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (currentUser == null) {
            return unauthorized("Vui lòng đăng nhập để xem thông báo");
        }

        List<NotificationResponse> responses = notificationService.getNotificationsByUser(currentUser.getId());
        return ok("Lấy danh sách thông báo thành công", responses);
    }


    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (currentUser == null) {
            return unauthorized("Vui lòng đăng nhập để thao tác");
        }

        notificationService.markAsRead(currentUser.getId(), id);
        return ok("Đánh dấu thông báo đã đọc thành công");
    }


    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (currentUser == null) {
            return unauthorized("Vui lòng đăng nhập");
        }

        notificationService.markAllAsRead(currentUser.getId());
        return ok("Đã đánh dấu tất cả thông báo là đã đọc");
    }
}
