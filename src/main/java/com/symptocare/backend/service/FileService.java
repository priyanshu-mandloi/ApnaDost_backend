package com.symptocare.backend.service;

import com.symptocare.backend.model.FileEntry;
import com.symptocare.backend.model.FileEntry.FileCategory;
import com.symptocare.backend.model.User;
import com.symptocare.backend.repository.FileRepository;
import com.symptocare.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;  // 🆕 add this

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Upload file
    public FileEntry upload(String email, MultipartFile file, String description) throws IOException {
        User user = getUser(email);

        // Create user-specific upload directory
        Path userUploadPath = Paths.get(uploadDir, String.valueOf(user.getId()));
        Files.createDirectories(userUploadPath);

        // Generate unique stored file name
        String originalFileName = file.getOriginalFilename();
        String extension = getExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + "." + extension;
        Path targetPath = userUploadPath.resolve(storedFileName);

        // Save file to disk
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Determine category from MIME type
        String mimeType = file.getContentType();
        FileCategory category = detectCategory(mimeType);

        // Format file size
        String sizeFormatted = formatSize(file.getSize());

        // Build and save FileEntry
        FileEntry entry = FileEntry.builder()
                .user(user)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .filePath(targetPath.toString())
                .fileType(mimeType)
                .fileSize(file.getSize())
                .fileSizeFormatted(sizeFormatted)
                .category(category)
                .description(description)
                .build();

        FileEntry saved = fileRepository.save(entry);
        log.info("File uploaded: {} for user: {}", storedFileName, email);
        return saved;
    }

    // Download file as Resource
    public Resource download(String email, Long fileId) throws MalformedURLException {
        User user = getUser(email);
        FileEntry entry = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Path filePath = Paths.get(entry.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found on disk");
        }

        return resource;
    }

    // Get all files for user
    public List<FileEntry> getAll(String email) {
        User user = getUser(email);
        return fileRepository.findByUserOrderByUploadedAtDesc(user);
    }

    // Get files by category
    public List<FileEntry> getByCategory(String email, FileCategory category) {
        User user = getUser(email);
        return fileRepository.findByUserAndCategoryOrderByUploadedAtDesc(user, category);
    }

    // Get only PDFs
    public List<FileEntry> getPdfs(String email) {
        User user = getUser(email);
        return fileRepository.findByUserAndFileTypeContainingOrderByUploadedAtDesc(user, "pdf");
    }

    // Search files by name
    public List<FileEntry> search(String email, String query) {
        User user = getUser(email);
        return fileRepository.searchByUser(user, query);
    }

    // Delete file
    public void delete(String email, Long fileId) throws IOException {
        User user = getUser(email);
        FileEntry entry = fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Delete from disk
        Path filePath = Paths.get(entry.getFilePath());
        Files.deleteIfExists(filePath);

        // Delete from DB
        fileRepository.delete(entry);
        log.info("File deleted: {} for user: {}", entry.getStoredFileName(), email);
    }

    // Extract text from PDF and cache it
   // Extract text from PDF and cache it
public String extractPdfText(String email, Long fileId) throws IOException {
    User user = getUser(email);
    FileEntry entry = fileRepository.findByIdAndUser(fileId, user)
            .orElseThrow(() -> new RuntimeException("File not found"));

    if (!entry.getFileType().contains("pdf")) {
        throw new RuntimeException("File is not a PDF");
    }

    // Return cached text if already extracted
    if (entry.getExtractedText() != null && !entry.getExtractedText().isEmpty()) {
        log.info("Returning cached PDF text for file: {}", fileId);
        return entry.getExtractedText();
    }

    // Extract text using PDFBox 3.x API
    File pdfFile = Paths.get(entry.getFilePath()).toFile();
    try (PDDocument document = Loader.loadPDF(pdfFile)) {  // ✅ fixed
        PDFTextStripper stripper = new PDFTextStripper();
        String extractedText = stripper.getText(document);

        // Cache extracted text in DB
        entry.setExtractedText(extractedText);
        entry.setUsedForChat(true);
        fileRepository.save(entry);

        log.info("PDF text extracted and cached for file: {}", fileId);
        return extractedText;
    }
}

    // Storage stats
    public Map<String, Object> getStorageStats(String email) {
        User user = getUser(email);
        Long totalBytes = fileRepository.totalStorageUsed(user);
        long totalFiles = fileRepository.countByUser(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", totalFiles);
        stats.put("totalBytes", totalBytes);
        stats.put("totalFormatted", formatSize(totalBytes));
        return stats;
    }

    // Get file entry (for internal use by PdfChatService)
    public FileEntry getFileEntry(String email, Long fileId) {
        User user = getUser(email);
        return fileRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    // --- Helpers ---

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "bin";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private FileCategory detectCategory(String mimeType) {
        if (mimeType == null) return FileCategory.OTHER;
        if (mimeType.contains("pdf") || mimeType.contains("word") ||
                mimeType.contains("excel") || mimeType.contains("text")) {
            return FileCategory.DOCUMENT;
        } else if (mimeType.startsWith("image/")) {
            return FileCategory.IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return FileCategory.VIDEO;
        } else if (mimeType.startsWith("audio/")) {
            return FileCategory.AUDIO;
        }
        return FileCategory.OTHER;
    }

    private String formatSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int unit = 0;
        double size = bytes;
        while (size >= 1024 && unit < units.length - 1) {
            size /= 1024;
            unit++;
        }
        return String.format("%.1f %s", size, units[unit]);
    }
}