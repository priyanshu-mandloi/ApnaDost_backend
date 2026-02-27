package com.symptocare.backend.service;

import com.symptocare.backend.model.Notification;
import com.symptocare.backend.model.Notification.NotificationType;
import com.symptocare.backend.model.User;
import com.symptocare.backend.repository.NotificationRepository;
import com.symptocare.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Optional — won't crash if WebSocket is not configured
    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    // Manual constructor without SimpMessagingTemplate
    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ─── Create & Push ────────────────────────────────────────────────────────

    public Notification createAndPush(User user,
                                      String title,
                                      String message,
                                      NotificationType type,
                                      Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Only push via WebSocket if it is available
        pushToUser(user.getEmail(), saved);

        log.info("Notification created → user: {} | type: {} | title: {}",
                user.getEmail(), type, title);

        return saved;
    }

    private void pushToUser(String email, Notification notification) {
        // Guard — if WebSocket not configured just skip silently
        if (messagingTemplate == null) {
            log.debug("WebSocket not configured — skipping push for user: {}", email);
            return;
        }

        try {
            messagingTemplate.convertAndSendToUser(
                    email,
                    "/queue/notifications",
                    Map.of(
                            "id", notification.getId(),
                            "title", notification.getTitle(),
                            "message", notification.getMessage(),
                            "type", notification.getType().name(),
                            "referenceId", notification.getReferenceId() != null
                                    ? notification.getReferenceId() : 0,
                            "createdAt", notification.getCreatedAt().toString(),
                            "isRead", false
                    )
            );
            log.debug("WebSocket push sent to user: {}", email);
        } catch (Exception e) {
            log.warn("WebSocket push failed for user: {} — {}", email, e.getMessage());
        }
    }

    // ─── Read Operations ──────────────────────────────────────────────────────

    public List<Notification> getAll(String email) {
        User user = getUser(email);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnread(String email) {
        User user = getUser(email);
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public List<Notification> getLatest(String email) {
        User user = getUser(email);
        return notificationRepository.findTop20ByUser(user);
    }

    public long getUnreadCount(String email) {
        User user = getUser(email);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // ─── Used by TaskScheduler ────────────────────────────────────────────────

    public boolean hasNotificationBeenSent(User user,
                                           Long referenceId,
                                           NotificationType type) {
        return notificationRepository
                .existsByUserAndReferenceIdAndType(user, referenceId, type);
    }

    // ─── Update Operations ────────────────────────────────────────────────────

    public Notification markAsRead(String email, Long id) {
        User user = getUser(email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String email) {
        User user = getUser(email);
        notificationRepository.markAllAsRead(user);
        log.info("All notifications marked as read for user: {}", email);
    }

    // ─── Delete Operations ────────────────────────────────────────────────────

    public void delete(String email, Long id) {
        User user = getUser(email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        notificationRepository.delete(notification);
    }

    public void cleanOldNotifications(User user) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        notificationRepository.deleteOldReadNotifications(user, oneMonthAgo);
        log.info("Old notifications cleaned for user: {}", user.getEmail());
    }
}