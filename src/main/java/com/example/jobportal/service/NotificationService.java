package com.example.jobportal.service;

import com.example.jobportal.dto.response.NotificationResponse;

import com.example.jobportal.model.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    void createNotification(Long userId, String title, String message, NotificationType type, Long refId, String refType);
    void createNotificationForRole(String roleName, String title, String message, NotificationType type, Long refId, String refType);
    List<NotificationResponse> getNotificationsByUser(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);
}
