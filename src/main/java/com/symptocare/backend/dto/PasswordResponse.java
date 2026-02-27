// src/main/java/com/symptocare/backend/dto/PasswordResponse.java
package com.symptocare.backend.dto;

import com.symptocare.backend.model.PasswordEntry;
import com.symptocare.backend.model.PasswordEntry.PasswordCategory;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PasswordResponse {

    private Long id;
    private String siteName;
    private String siteUrl;
    private String username;
    private String password; // decrypted — only shown when explicitly requested
    private String notes;
    private String iconUrl;
    private PasswordCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PasswordResponse from(PasswordEntry entry, String decryptedPassword) {
        PasswordResponse res = new PasswordResponse();
        res.setId(entry.getId());
        res.setSiteName(entry.getSiteName());
        res.setSiteUrl(entry.getSiteUrl());
        res.setUsername(entry.getUsername());
        res.setPassword(decryptedPassword);
        res.setNotes(entry.getNotes());
        res.setIconUrl(entry.getIconUrl());
        res.setCategory(entry.getCategory());
        res.setCreatedAt(entry.getCreatedAt());
        res.setUpdatedAt(entry.getUpdatedAt());
        return res;
    }

    // Use this when listing — password is masked
    public static PasswordResponse fromMasked(PasswordEntry entry) {
        return from(entry, "••••••••");
    }
}