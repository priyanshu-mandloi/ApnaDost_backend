package com.symptocare.backend.repository;

import com.symptocare.backend.model.FileEntry;
import com.symptocare.backend.model.FileEntry.FileCategory;
import com.symptocare.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntry, Long> {

    List<FileEntry> findByUserOrderByUploadedAtDesc(User user);

    List<FileEntry> findByUserAndCategoryOrderByUploadedAtDesc(User user, FileCategory category);

    // All PDFs for this user
    List<FileEntry> findByUserAndFileTypeContainingOrderByUploadedAtDesc(User user, String fileType);

    Optional<FileEntry> findByIdAndUser(Long id, User user);

    Optional<FileEntry> findByStoredFileName(String storedFileName);

    // Search by original file name
    @Query("SELECT f FROM FileEntry f WHERE f.user = :user AND " +
           "LOWER(f.originalFileName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY f.uploadedAt DESC")
    List<FileEntry> searchByUser(@Param("user") User user, @Param("query") String query);

    // Total storage used by user in bytes
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileEntry f WHERE f.user = :user")
    Long totalStorageUsed(@Param("user") User user);

    long countByUser(User user);

    // All files that have been used for chat (have extracted text)
    List<FileEntry> findByUserAndUsedForChatTrueOrderByUploadedAtDesc(User user);
}