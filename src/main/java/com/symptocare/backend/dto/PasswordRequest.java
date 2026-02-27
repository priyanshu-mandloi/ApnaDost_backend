// src/main/java/com/symptocare/backend/dto/PasswordRequest.java
package com.symptocare.backend.dto;

import com.symptocare.backend.model.PasswordEntry.PasswordCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PasswordRequest {

    @NotBlank(message = "Site name is required")
    private String siteName;

    private String siteUrl;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String notes;

    private String iconUrl;

    @NotNull(message = "Category is required")
    private PasswordCategory category;
}