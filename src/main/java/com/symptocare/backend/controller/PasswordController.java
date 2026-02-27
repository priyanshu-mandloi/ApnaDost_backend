// src/main/java/com/symptocare/backend/controller/PasswordController.java
package com.symptocare.backend.controller;

import com.symptocare.backend.dto.PasswordRequest;
import com.symptocare.backend.dto.PasswordResponse;
import com.symptocare.backend.model.PasswordEntry.PasswordCategory;
import com.symptocare.backend.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    // POST /api/passwords → create
    @PostMapping
    public ResponseEntity<PasswordResponse> create(
            Authentication auth,
            @Valid @RequestBody PasswordRequest request) {
        return ResponseEntity.ok(passwordService.create(auth.getName(), request));
    }

    // GET /api/passwords → get all (masked)
    @GetMapping
    public ResponseEntity<List<PasswordResponse>> getAll(Authentication auth) {
        return ResponseEntity.ok(passwordService.getAll(auth.getName()));
    }

    // GET /api/passwords/category?type=BANKING → filter by category
    @GetMapping("/category")
    public ResponseEntity<List<PasswordResponse>> getByCategory(
            Authentication auth,
            @RequestParam PasswordCategory type) {
        return ResponseEntity.ok(passwordService.getByCategory(auth.getName(), type));
    }

    // GET /api/passwords/search?q=gmail → search
    @GetMapping("/search")
    public ResponseEntity<List<PasswordResponse>> search(
            Authentication auth,
            @RequestParam String q) {
        return ResponseEntity.ok(passwordService.search(auth.getName(), q));
    }

    // GET /api/passwords/{id}/reveal → get decrypted password
    @GetMapping("/{id}/reveal")
    public ResponseEntity<PasswordResponse> reveal(
            Authentication auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(passwordService.reveal(auth.getName(), id));
    }

    // PUT /api/passwords/{id} → update
    @PutMapping("/{id}")
    public ResponseEntity<PasswordResponse> update(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody PasswordRequest request) {
        return ResponseEntity.ok(passwordService.update(auth.getName(), id, request));
    }

    // DELETE /api/passwords/{id} → delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            Authentication auth,
            @PathVariable Long id) {
        passwordService.delete(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Password deleted successfully"));
    }

    // GET /api/passwords/count → total count
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCount(Authentication auth) {
        return ResponseEntity.ok(Map.of("total", passwordService.getCount(auth.getName())));
    }
}