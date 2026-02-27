package com.symptocare.backend.repository;

import com.symptocare.backend.model.Notification;
import com.symptocare.backend.model.Notification.NotificationType;
import com.symptocare.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for user latest first
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Only unread notifications
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // Filter by type
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

    // Count unread
    long countByUserAndIsReadFalse(User user);

    // Mark all as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsRead(@Param("user") User user);

    // Delete old read notifications older than given date
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.isRead = true AND n.createdAt < :before")
    void deleteOldReadNotifications(@Param("user") User user, @Param("before") LocalDateTime before);

    // Check if notification already sent for a task (avoid duplicates)
    boolean existsByUserAndReferenceIdAndType(User user, Long referenceId, NotificationType type);

    // Latest 20 for bell icon dropdown
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findTop20ByUser(@Param("user") User user);
}