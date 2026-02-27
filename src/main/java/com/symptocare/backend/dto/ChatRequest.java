package com.symptocare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {

    // The PDF file ID to chat with
    @NotNull(message = "File ID is required")
    private Long fileId;

    // The user's question
    @NotBlank(message = "Question is required")
    private String question;

    // Previous conversation history
    // Each map has "role" (user/assistant) and "content"
    private List<Map<String, String>> history;
}