package com.symptocare.backend.controller;

import com.symptocare.backend.model.Notification;
import com.symptocare.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications → all notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getAll(Authentication auth) {
        return ResponseEntity.ok(notificationService.getAll(auth.getName()));
    }

    // GET /api/notifications/latest → top 20 for bell icon
    @GetMapping("/latest")
    public ResponseEntity<List<Notification>> getLatest(Authentication auth) {
        return ResponseEntity.ok(notificationService.getLatest(auth.getName()));
    }

    // GET /api/notifications/unread → only unread
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnread(Authentication auth) {
        return ResponseEntity.ok(notificationService.getUnread(auth.getName()));
    }

    // GET /api/notifications/count → unread count for badge
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        return ResponseEntity.ok(
                Map.of("unreadCount", notificationService.getUnreadCount(auth.getName()))
        );
    }

    // PATCH /api/notifications/{id}/read → mark single as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            Authentication auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(auth.getName(), id));
    }

    // PATCH /api/notifications/read-all → mark all as read
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication auth) {
        notificationService.markAllAsRead(auth.getName());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    // DELETE /api/notifications/{id} → delete single
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            Authentication auth,
            @PathVariable Long id) {
        notificationService.delete(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }
}