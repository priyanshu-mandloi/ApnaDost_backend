package com.symptocare.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final WebClient groqWebClient;

    @Value("${groq.model}")
    private String model;

    public String chat(List<Map<String, String>> messages) {
        try {

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("max_tokens", 512);
            body.put("temperature", 0.5);

            Map response = groqWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.isError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Groq error response: {}", errorBody);
                                        return Mono.error(new RuntimeException(errorBody));
                                    }))
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("choices")) {
                throw new RuntimeException("Invalid response from Groq: " + response);
            }

            List choices = (List) response.get("choices");
            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Groq API error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage());
        }
    }

    public String getMotivationalQuote(String taskTitle) {
        try {

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "You are ApnaDost, a friendly Indian mentor. " +
                    "Give a short motivational message in 1-2 sentences. " +
                    "Be warm and encouraging.");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content",
                    "My task is: '" + taskTitle + "'. Give me motivation.");
            messages.add(userMsg);

            return chat(messages);

        } catch (Exception e) {
            return "Chalo " + taskTitle + " complete karte hain! You've got this! 💪";
        }
    }

    public String askAboutPdf(String pdfText,
                              String userQuestion,
                              List<Map<String, String>> history) {

        if (pdfText == null || pdfText.trim().isEmpty()) {
            throw new RuntimeException("PDF text is empty");
        }

        String cleanedText = pdfText
                .replaceAll("[^\\x00-\\x7F]", "")
                .replaceAll("\\s+", " ")
                .trim();

        int maxCharacters = 4000;

        if (cleanedText.length() > maxCharacters) {
            cleanedText = cleanedText.substring(0, maxCharacters);
        }

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "You are a helpful AI assistant. " +
                "Answer only using the provided document. " +
                "If the answer is not found, say you don't know.");
        messages.add(systemMsg);

        Map<String, String> documentMsg = new HashMap<>();
        documentMsg.put("role", "user");
        documentMsg.put("content",
                "Document:\n" + cleanedText);
        messages.add(documentMsg);

        if (history != null) {
            for (Map<String, String> msg : history) {

                String role = msg.get("role");
                String content = msg.get("content");

                if (role != null &&
                        content != null &&
                        !content.trim().isEmpty() &&
                        (role.equals("user") || role.equals("assistant"))) {

                    Map<String, String> historyMsg = new HashMap<>();
                    historyMsg.put("role", role);
                    historyMsg.put("content", content);
                    messages.add(historyMsg);
                }
            }
        }

        Map<String, String> questionMsg = new HashMap<>();
        questionMsg.put("role", "user");
        questionMsg.put("content", userQuestion);
        messages.add(questionMsg);

        return chat(messages);
    }
}