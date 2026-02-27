// src/main/java/com/symptocare/backend/repository/PasswordRepository.java
package com.symptocare.backend.repository;

import com.symptocare.backend.model.PasswordEntry;
import com.symptocare.backend.model.PasswordEntry.PasswordCategory;
import com.symptocare.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PasswordRepository extends JpaRepository<PasswordEntry, Long> {

    // Get all passwords for a user sorted by site name
    List<PasswordEntry> findByUserOrderBySiteNameAsc(User user);

    // Filter by category
    List<PasswordEntry> findByUserAndCategoryOrderBySiteNameAsc(User user, PasswordCategory category);

    // Search by site name or username
    @Query("SELECT p FROM PasswordEntry p WHERE p.user = :user AND " +
           "(LOWER(p.siteName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.username) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<PasswordEntry> searchByUser(@Param("user") User user, @Param("query") String query);

    // Find specific entry belonging to user (security check)
    Optional<PasswordEntry> findByIdAndUser(Long id, User user);

    // Count total passwords per user
    long countByUser(User user);
}