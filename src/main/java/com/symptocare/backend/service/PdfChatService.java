package com.symptocare.backend.service;

import com.symptocare.backend.dto.ChatRequest;
import com.symptocare.backend.model.FileEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfChatService {

    private final FileService fileService;
    private final OpenAIService openAIService;

    public Map<String, Object> chat(String email, ChatRequest request) throws IOException {

        // 1. Validate the file belongs to user and is a PDF
        FileEntry fileEntry = fileService.getFileEntry(email, request.getFileId());

        if (!fileEntry.getFileType().contains("pdf")) {
            throw new RuntimeException("Only PDF files are supported for chat");
        }

        // 2. Extract (or fetch cached) PDF text
        String pdfText = fileService.extractPdfText(email, request.getFileId());

        if (pdfText == null || pdfText.trim().isEmpty()) {
            throw new RuntimeException("Could not extract text from PDF. " +
                    "The PDF might be scanned/image-based.");
        }

        // 3. Build conversation history for context
        List<Map<String, String>> history = new ArrayList<>();
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            // Only keep last 6 messages to avoid token overflow
            List<Map<String, String>> fullHistory = request.getHistory();
            int startIndex = Math.max(0, fullHistory.size() - 6);
            history = fullHistory.subList(startIndex, fullHistory.size());
        }

        // 4. Ask Groq AI with PDF context
        String answer = openAIService.askAboutPdf(pdfText, request.getQuestion(), history);

        log.info("PDF chat completed for file: {} by user: {}", request.getFileId(), email);

        // 5. Return question + answer + file info
        return Map.of(
                "fileId", request.getFileId(),
                "fileName", fileEntry.getOriginalFileName(),
                "question", request.getQuestion(),
                "answer", answer
        );
    }

    // Just extract text without chatting (prep for chat)
    public Map<String, Object> prepareForChat(String email, Long fileId) throws IOException {
        FileEntry fileEntry = fileService.getFileEntry(email, fileId);

        if (!fileEntry.getFileType().contains("pdf")) {
            throw new RuntimeException("Only PDF files are supported");
        }

        String text = fileService.extractPdfText(email, fileId);
        int wordCount = text.trim().split("\\s+").length;
        int charCount = text.length();

        return Map.of(
                "fileId", fileId,
                "fileName", fileEntry.getOriginalFileName(),
                "wordCount", wordCount,
                "charCount", charCount,
                "ready", true,
                "message", "PDF is ready for chat! It has " + wordCount + " words."
        );
    }
}