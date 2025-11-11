package com.example.jobportal.service;

import com.example.jobportal.dto.response.NotificationResponse;
import com.example.jobportal.model.entity.Notification;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.repository.NotificationRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void createNotification(Long userId, String title, String message, String type, Long refId, String refType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        createNotification(title, message, type, refId, refType, user);
    }

    @Override
    @Transactional
    public void createNotificationForRole(String roleName, String title, String message, String type, Long refId, String refType) {
        List<User> users = userRepository.findAllByRoleName(roleName);
        for (User user : users) {
            createNotification(title, message, type, refId, refType, user);
        }
    }

    private void createNotification(String title, String message, String type, Long refId, String refType, User user) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(refId)
                .referenceType(refType)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        messagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/queue/notifications",
                NotificationResponse.fromEntity(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
