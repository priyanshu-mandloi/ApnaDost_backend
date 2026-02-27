package com.symptocare.backend.controller;

import com.symptocare.backend.dto.ChatRequest;
import com.symptocare.backend.service.PdfChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final PdfChatService pdfChatService;

    // POST /api/chat/pdf → chat with a PDF
    @PostMapping("/pdf")
    public ResponseEntity<Map<String, Object>> chatWithPdf(
            Authentication auth,
            @Valid @RequestBody ChatRequest request) throws IOException {
        return ResponseEntity.ok(pdfChatService.chat(auth.getName(), request));
    }

    // GET /api/chat/pdf/{fileId}/prepare → extract text and prepare PDF for chat
    @GetMapping("/pdf/{fileId}/prepare")
    public ResponseEntity<Map<String, Object>> preparePdf(
            Authentication auth,
            @PathVariable Long fileId) throws IOException {
        return ResponseEntity.ok(pdfChatService.prepareForChat(auth.getName(), fileId));
    }
}