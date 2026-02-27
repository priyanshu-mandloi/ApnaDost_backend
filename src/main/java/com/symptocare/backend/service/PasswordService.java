// src/main/java/com/symptocare/backend/service/PasswordService.java
package com.symptocare.backend.service;

import com.symptocare.backend.dto.PasswordRequest;
import com.symptocare.backend.dto.PasswordResponse;
import com.symptocare.backend.model.PasswordEntry;
import com.symptocare.backend.model.PasswordEntry.PasswordCategory;
import com.symptocare.backend.model.User;
import com.symptocare.backend.repository.PasswordRepository;
import com.symptocare.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordRepository passwordRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Create new password entry
    public PasswordResponse create(String email, PasswordRequest request) {
        User user = getUser(email);

        PasswordEntry entry = PasswordEntry.builder()
                .user(user)
                .siteName(request.getSiteName())
                .siteUrl(request.getSiteUrl())
                .username(request.getUsername())
                .encryptedPassword(encryptionService.encrypt(request.getPassword()))
                .notes(request.getNotes())
                .iconUrl(request.getIconUrl())
                .category(request.getCategory())
                .build();

        return PasswordResponse.fromMasked(passwordRepository.save(entry));
    }

    // Get all passwords — masked
    public List<PasswordResponse> getAll(String email) {
        User user = getUser(email);
        return passwordRepository.findByUserOrderBySiteNameAsc(user)
                .stream()
                .map(PasswordResponse::fromMasked)
                .collect(Collectors.toList());
    }

    // Get by category — masked
    public List<PasswordResponse> getByCategory(String email, PasswordCategory category) {
        User user = getUser(email);
        return passwordRepository.findByUserAndCategoryOrderBySiteNameAsc(user, category)
                .stream()
                .map(PasswordResponse::fromMasked)
                .collect(Collectors.toList());
    }

    // Search by site name or username — masked
    public List<PasswordResponse> search(String email, String query) {
        User user = getUser(email);
        return passwordRepository.searchByUser(user, query)
                .stream()
                .map(PasswordResponse::fromMasked)
                .collect(Collectors.toList());
    }

    // Reveal password for a specific entry — decrypted
    public PasswordResponse reveal(String email, Long id) {
        User user = getUser(email);
        PasswordEntry entry = passwordRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Password entry not found"));

        String decrypted = encryptionService.decrypt(entry.getEncryptedPassword());
        return PasswordResponse.from(entry, decrypted);
    }

    // Update password entry
    public PasswordResponse update(String email, Long id, PasswordRequest request) {
        User user = getUser(email);
        PasswordEntry entry = passwordRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Password entry not found"));

        entry.setSiteName(request.getSiteName());
        entry.setSiteUrl(request.getSiteUrl());
        entry.setUsername(request.getUsername());
        entry.setEncryptedPassword(encryptionService.encrypt(request.getPassword()));
        entry.setNotes(request.getNotes());
        entry.setIconUrl(request.getIconUrl());
        entry.setCategory(request.getCategory());

        return PasswordResponse.fromMasked(passwordRepository.save(entry));
    }

    // Delete password entry
    public void delete(String email, Long id) {
        User user = getUser(email);
        PasswordEntry entry = passwordRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Password entry not found"));
        passwordRepository.delete(entry);
    }

    // Get total count
    public long getCount(String email) {
        User user = getUser(email);
        return passwordRepository.countByUser(user);
    }
}