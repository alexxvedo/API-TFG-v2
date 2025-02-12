package com.example.api_v2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;

    public GeminiClient(@Value("AIzaSyDzXX45x2SwbJxrbrXx9pwmFGEY1cpT9ks") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent")
                .build();
    }

    public Mono<String> getResponseFromGemini(String prompt) {
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(
                        Retry.fixedDelay(3, Duration.ofSeconds(2))  // 🔹 Reintenta 3 veces con 2s de espera
                                .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests)
                )
                .map(response -> {
                    try {
                        return extractTextFromResponse(response);
                    } catch (Exception e) {
                        throw new RuntimeException("Error extracting text from Gemini response", e);
                    }
                });
    }

    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<String, Object> firstCandidate = (Map<String, Object>) candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Invalid response format from Gemini API", e);
        }
    }
}

