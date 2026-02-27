// src/main/java/com/symptocare/backend/model/PasswordEntry.java
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
@Table(name = "password_entries")
public class PasswordEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // e.g. "Gmail", "Netflix", "HDFC Bank"
    @Column(nullable = false)
    private String siteName;

    // e.g. "https://gmail.com"
    private String siteUrl;

    // Username or email used on that site
    @Column(nullable = false)
    private String username;

    // AES encrypted password - never stored as plain text
    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedPassword;

    // Optional notes e.g. "recovery email is xyz@gmail.com"
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Category e.g. SOCIAL, BANKING, WORK, ENTERTAINMENT, OTHER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PasswordCategory category;

    // Favicon or icon URL for the site (optional, frontend can use it)
    private String iconUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum PasswordCategory {
        SOCIAL,
        BANKING,
        WORK,
        ENTERTAINMENT,
        SHOPPING,
        OTHER
    }
}