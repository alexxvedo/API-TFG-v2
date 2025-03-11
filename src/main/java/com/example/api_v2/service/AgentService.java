package com.example.api_v2.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AgentService {
    private final WebClient webClient;

    public AgentService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")  // Conecta con agent.py
                .build(); 
    }

    // Enviar documento al agente para procesamiento
    public Mono<Map<String, Object>> processDocument(byte[] fileContent) {
        // Convertir el contenido del archivo a Base64
        String base64Content = Base64.getEncoder().encodeToString(fileContent);
        
        return webClient.post()
                .uri("/process-document/")
                .bodyValue(Map.of(
                        "content", base64Content
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    // Generar flashcards desde un documento
    public Mono<List<Map<String, Object>>> generateFlashcardsFromDocument(String collectionId, String documentId, int numFlashcards) {
        return webClient.get()
                .uri("/agent/{collectionId}/flashcards/{documentId}?num_flashcards={numFlashcards}",
                     collectionId, documentId, numFlashcards)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    // Generar flashcards desde una colección
    public Mono<List<Map<String, Object>>> generateFlashcardsFromCollection(String collectionId, int numFlashcards) {
        return webClient.get()
                .uri("/generate-flashcards/{collectionId}?num_flashcards={numFlashcards}",
                     collectionId, numFlashcards)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    // Preguntar al agente
    public Mono<Map<String, Object>> askAgent(String collectionId, String question) {
        return webClient.post()
                .uri("/ask-agent/")
                .bodyValue(Map.of(
                        "collection_id", collectionId,
                        "question", question
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }


    public Mono<Map<String, Object>> getBriefSummary(String collectionId, String documentId) {
        return webClient.get()
                .uri("/brief-summary/{collectionId}/{documentId}",
                     collectionId, documentId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> getLongSummary(String collectionId, String documentId){
        return webClient.get()
                .uri("/long-summary/{collectionId}/{documentId}",
                     collectionId, documentId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
