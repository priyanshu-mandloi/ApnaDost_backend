package com.symptocare.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // Type of notification
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // Reference ID — taskId, expenseId etc depending on type
    private Long referenceId;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public enum NotificationType {
        TASK_REMINDER,       // Task time has arrived
        TASK_OVERDUE,        // Task was not completed and time passed
        MOTIVATIONAL,        // Random motivational push
        EXPENSE_ALERT,       // Monthly expense exceeded threshold
        SYSTEM               // General system notification
    }
}