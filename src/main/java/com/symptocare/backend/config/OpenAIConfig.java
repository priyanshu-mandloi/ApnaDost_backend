package com.symptocare.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAIConfig {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.base.url}")
    private String groqBaseUrl;

    @Bean
    public WebClient groqWebClient() {
        return WebClient.builder()
                .baseUrl(groqBaseUrl)
                .defaultHeader("Authorization", "Bearer " + groqApiKey)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer
                .build();
    }
}