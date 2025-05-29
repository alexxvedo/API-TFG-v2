package com.example.api_v2.controller;

import com.example.api_v2.service.AgentService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

        private final AgentService agentService;

        @PostMapping("/{collectionId}/flashcards/{documentId}")
        public Mono<ResponseEntity<List<Map<String, Object>>>> generateFlashcardsFromDocument(
                        @PathVariable("collectionId") String collectionId,
                        @PathVariable("documentId") String documentId,
                        @RequestBody Map<String, Object> requestBody) {
                Integer numFlashcards = requestBody != null
                                ? (Integer) requestBody.getOrDefault("numFlashcards", 5)
                                : 5;
                return agentService
                                .generateFlashcardsFromDocument(
                                                collectionId,
                                                documentId,
                                                numFlashcards)
                                .map(ResponseEntity::ok);
        }

        @GetMapping("/{collectionId}/flashcards")
        public Mono<ResponseEntity<List<Map<String, Object>>>> generateFlashcardsFromCollection(
                        @PathVariable("collectionId") String collectionId,
                        @RequestParam(defaultValue = "5") int numFlashcards) {
                return agentService
                                .generateFlashcardsFromCollection(collectionId, numFlashcards)
                                .map(ResponseEntity::ok);
        }

        @GetMapping("/{collectionId}/brief-summary/{documentId}")
        public Mono<ResponseEntity<Map<String, Object>>> getBriefSummary(
                        @PathVariable("collectionId") String collectionId,
                        @PathVariable("documentId") String documentId) {
                return agentService
                                .getBriefSummary(collectionId, documentId)
                                .map(ResponseEntity::ok);
        }

        @GetMapping("/{collectionId}/long-summary/{documentId}")
        public Mono<ResponseEntity<Map<String, Object>>> getLongSummary(
                        @PathVariable("collectionId") String collectionId,
                        @PathVariable("documentId") String documentId,
                        @RequestParam(value = "desiredLength", defaultValue = "50") int desiredLength) {
                return agentService
                                .getLongSummary(collectionId, documentId, desiredLength)
                                .map(ResponseEntity::ok);
        }

        @PostMapping("/ask-agent")
        public Mono<ResponseEntity<Map<String, Object>>> askAgent(
                        @RequestBody Map<String, Object> requestBody) {
                // Convertir de forma segura los valores a String
                String collectionId = requestBody.get("collectionId") != null
                                ? String.valueOf(requestBody.get("collectionId"))
                                : null;
                String question = requestBody.get("question") != null ? String.valueOf(requestBody.get("question"))
                                : null;
                String additionalContext = requestBody.get("additional_context") != null
                                ? String.valueOf(requestBody.get("additional_context"))
                                : null;

                // Obtener el historial de conversación si existe
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> conversationHistory = requestBody.get("conversation_history") != null
                                ? (List<Map<String, Object>>) requestBody.get("conversation_history")
                                : null;

                System.out.println("Recibida petición askAgent con collectionId: " + collectionId + ", question: "
                                + question);
                if (conversationHistory != null) {
                        System.out.println("Historial de conversación recibido con " + conversationHistory.size()
                                        + " mensajes");
                }

                return agentService
                                .askAgent(collectionId, question, additionalContext, conversationHistory)
                                .map(ResponseEntity::ok);
        }
}
