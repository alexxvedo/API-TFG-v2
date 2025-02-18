package com.example.api_v2.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class FaissService {
    private final WebClient webClient;

    public FaissService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")  // API de FAISS en Python
                .build();
    }

    public Mono<String> addDocument(String id, String text) {
        return webClient.post()
                .uri("/add-document/")
                .bodyValue(Map.of("id", id, "text", text))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<Map<String, Object>> searchDocument(String query, int topK) {
        return webClient.post()
                .uri("/search-document/")
                .bodyValue(Map.of("text", query, "top_k", topK))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
