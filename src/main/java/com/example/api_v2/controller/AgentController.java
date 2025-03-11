package com.example.api_v2.controller;

import com.example.api_v2.service.AgentService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    

    @PostMapping("/{collectionId}/flashcards/{documentId}")
    public Mono<
        ResponseEntity<List<Map<String, Object>>>
    > generateFlashcardsFromDocument(
        @PathVariable("collectionId") String collectionId,
        @PathVariable("documentId") String documentId,
        @RequestBody Map<String, Object> requestBody
    ) {
        Integer numFlashcards = requestBody != null
            ? (Integer) requestBody.getOrDefault("numFlashcards", 5)
            : 5;
        return agentService
            .generateFlashcardsFromDocument(
                collectionId,
                documentId,
                numFlashcards
            )
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{collectionId}/flashcards")
    public Mono<
        ResponseEntity<List<Map<String, Object>>>
    > generateFlashcardsFromCollection(
        @PathVariable("collectionId") String collectionId,
        @RequestParam(defaultValue = "5") int numFlashcards
    ) {
        return agentService
            .generateFlashcardsFromCollection(collectionId, numFlashcards)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{collectionId}/brief-summary/{documentId}")
    public Mono<ResponseEntity<Map<String, Object>>> getBriefSummary(
        @PathVariable("collectionId") String collectionId,
        @PathVariable("documentId") String documentId
    ) {
        return agentService
            .getBriefSummary(collectionId, documentId)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{collectionId}/long-summary/{documentId}")
    public Mono<ResponseEntity<Map<String, Object>>> getLongSummary(
        @PathVariable("collectionId") String collectionId,
        @PathVariable("documentId") String documentId
    ) {
        return agentService
            .getLongSummary(collectionId, documentId)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/ask-agent")
    public Mono<ResponseEntity<Map<String, Object>>> askAgent(
        @RequestParam("collectionId") String collectionId,
        @RequestParam("question") String question
    ) {
        return agentService
            .askAgent(collectionId, question)
            .map(ResponseEntity::ok);
    }
}
