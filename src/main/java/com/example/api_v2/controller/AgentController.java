package com.example.api_v2.controller;

import com.example.api_v2.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/process-document")
    public Mono<ResponseEntity<Map<String, Object>>> processDocument(
            @RequestParam String documentId,
            @RequestParam String collectionId,
            @RequestParam MultipartFile file) throws IOException {

        byte[] fileContent = file.getBytes();
        return agentService.processDocument(documentId, collectionId, fileContent)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{collectionId}/flashcards/{documentId}")
    public Mono<ResponseEntity<List<Map<String, Object>>>> generateFlashcardsFromDocument(
            @PathVariable String collectionId,
            @PathVariable String documentId,
            @RequestBody Map<String, Object> requestBody) {
        Integer numFlashcards = requestBody != null ? 
            (Integer) requestBody.getOrDefault("numFlashcards", 5) : 5;
        return agentService.generateFlashcardsFromDocument(collectionId, documentId, numFlashcards)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{collectionId}/flashcards")
    public Mono<ResponseEntity<List<Map<String, Object>>>> generateFlashcardsFromCollection(
            @PathVariable String collectionId,
            @RequestParam(defaultValue = "5") int numFlashcards) {

        return agentService.generateFlashcardsFromCollection(collectionId, numFlashcards)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{collectionId}/brief-summary/{documentId}")
    public Mono<ResponseEntity<Map<String, Object>>> getBriefSummary(
            @PathVariable String collectionId,
            @PathVariable String documentId) {

        return agentService.getBriefSummary(collectionId, documentId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{collectionId}/long-summary/{documentId}")
    public Mono<ResponseEntity<Map<String, Object>>> getLongSummary(
            @PathVariable String collectionId,
            @PathVariable String documentId) {

        return agentService.getLongSummary(collectionId, documentId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/ask-agent")
    public Mono<ResponseEntity<Map<String, Object>>> askAgent(
            @RequestParam String collectionId,
            @RequestParam String question) {

        return agentService.askAgent(collectionId, question)
                .map(ResponseEntity::ok);
    }
}
