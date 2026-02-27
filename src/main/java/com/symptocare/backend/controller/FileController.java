package com.symptocare.backend.controller;

import com.symptocare.backend.model.FileEntry;
import com.symptocare.backend.model.FileEntry.FileCategory;
import com.symptocare.backend.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // POST /api/files/upload
    @PostMapping("/upload")
    public ResponseEntity<FileEntry> upload(
            Authentication auth,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description)
            throws IOException {
        return ResponseEntity.ok(fileService.upload(auth.getName(), file, description));
    }

    // GET /api/files
    @GetMapping
    public ResponseEntity<List<FileEntry>> getAll(Authentication auth) {
        return ResponseEntity.ok(fileService.getAll(auth.getName()));
    }

    // GET /api/files/category?type=DOCUMENT
    @GetMapping("/category")
    public ResponseEntity<List<FileEntry>> getByCategory(
            Authentication auth,
            @RequestParam FileCategory type) {
        return ResponseEntity.ok(fileService.getByCategory(auth.getName(), type));
    }

    // GET /api/files/pdfs
    @GetMapping("/pdfs")
    public ResponseEntity<List<FileEntry>> getPdfs(Authentication auth) {
        return ResponseEntity.ok(fileService.getPdfs(auth.getName()));
    }

    // GET /api/files/search?q=resume
    @GetMapping("/search")
    public ResponseEntity<List<FileEntry>> search(
            Authentication auth,
            @RequestParam String q) {
        return ResponseEntity.ok(fileService.search(auth.getName(), q));
    }

    // GET /api/files/{id}/download
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            Authentication auth,
            @PathVariable Long id) throws IOException {

        Resource resource = fileService.download(auth.getName(), id);
        FileEntry entry = fileService.getFileEntry(auth.getName(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entry.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + entry.getOriginalFileName() + "\"")
                .body(resource);
    }

    // DELETE /api/files/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            Authentication auth,
            @PathVariable Long id) throws IOException {
        fileService.delete(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    // GET /api/files/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication auth) {
        return ResponseEntity.ok(fileService.getStorageStats(auth.getName()));
    }
}