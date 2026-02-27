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
@Table(name = "file_entries")
public class FileEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // Original file name e.g. "resume.pdf"
    @Column(nullable = false)
    private String originalFileName;
    // Stored file name (UUID based to avoid conflicts) e.g. "a1b2c3-resume.pdf"
    @Column(nullable = false, unique = true)
    private String storedFileName;
    // Full path on server e.g. "uploads/userId/a1b2c3-resume.pdf"
    @Column(nullable = false, columnDefinition = "TEXT")
    private String filePath;
    // MIME type e.g. "application/pdf", "image/png", "video/mp4"
    @Column(nullable = false)
    private String fileType;
    // File size in bytes
    @Column(nullable = false)
    private Long fileSize;
    // Human readable size e.g. "2.3 MB"
    private String fileSizeFormatted;
    // Category for filtering
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;
    // Optional description or tag added by user
    @Column(columnDefinition = "TEXT")
    private String description;
    // Whether this file was used for PDF chat feature
    @Column(nullable = false)
    private boolean usedForChat;
    // If PDF chat was done, store extracted text here for re-use
    @Column(columnDefinition = "TEXT")
    private String extractedText;
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
        this.usedForChat = false;
    }
    public enum FileCategory {
        DOCUMENT,    // PDF, Word, Excel
        IMAGE,       // PNG, JPG, etc
        VIDEO,       // MP4, AVI
        AUDIO,       // MP3, WAV
        OTHER
    }
}