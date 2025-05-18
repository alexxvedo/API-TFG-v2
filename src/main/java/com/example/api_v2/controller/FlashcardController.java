package com.example.api_v2.controller;

import com.example.api_v2.dto.FlashcardDto;
import com.example.api_v2.dto.FlashcardGenerationDto;
import com.example.api_v2.dto.FlashcardReviewDto;
import com.example.api_v2.dto.FlashcardStatsDto;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.AIService;
import com.example.api_v2.service.CollectionService;
import com.example.api_v2.service.FlashcardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/collections/{collectionId}/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final AIService aiService;
    private final CollectionService collectionService;

    public FlashcardController(FlashcardService flashcardService, AIService aiService,
            CollectionService collectionService) {
        this.flashcardService = flashcardService;
        this.aiService = aiService;
        this.collectionService = collectionService;
    }

    @GetMapping
    @WorkspaceAccess
    public ResponseEntity<List<FlashcardDto>> getFlashcardsByCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId) {
        log.info("Obteniendo flashcards para la colección: {} en workspace: {}", collectionId, workspaceId);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        return ResponseEntity.ok(flashcardService.getFlashcardsByCollection(collectionId));
    }

    @GetMapping("/review")
    @WorkspaceAccess
    public ResponseEntity<List<Flashcard>> getFlashcardsForReview(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId) {
        log.info("Obteniendo flashcards para revisión de la colección: {} en workspace: {}", collectionId, workspaceId);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        List<Flashcard> flashcards = flashcardService.getFlashcardsForReview(collectionId);
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/stats")
    @WorkspaceAccess
    public ResponseEntity<FlashcardStatsDto> getFlashcardStats(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId) {
        log.info("Obteniendo estadísticas de flashcards para la colección: {} en workspace: {}", collectionId,
                workspaceId);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        return ResponseEntity.ok(flashcardService.getFlashcardStats(collectionId));
    }

    @PostMapping("/user/{email}")
    @WorkspaceEditAccess
    public ResponseEntity<FlashcardDto> createFlashcard(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestBody FlashcardDto flashcardDto,
            @PathVariable("email") String email) {
        log.info("Creando flashcard en colección {} por usuario {}: {}", collectionId, email, flashcardDto);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        return ResponseEntity.ok(flashcardService.createFlashcard(collectionId, flashcardDto, email));
    }

    @PostMapping("/generate")
    @WorkspaceEditAccess
    public ResponseEntity<Map<String, Object>> generateFlashcards(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestBody FlashcardGenerationDto request) {
        log.info("Generando flashcards para la colección {}: {}", collectionId, request);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        Map<String, Object> result = aiService.generateFlashcards(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{flashcardId}")
    @WorkspaceEditAccess
    public ResponseEntity<FlashcardDto> updateFlashcard(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("flashcardId") Long flashcardId,
            @RequestBody FlashcardDto flashcardDto) {
        log.info("Actualizando flashcard {} en colección {}: {}", flashcardId, collectionId, flashcardDto);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        return ResponseEntity.ok(flashcardService.updateFlashcard(flashcardId, flashcardDto));
    }

    @DeleteMapping("/{flashcardId}")
    @WorkspaceEditAccess
    public ResponseEntity<Void> deleteFlashcard(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("flashcardId") Long flashcardId) {
        log.info("Eliminando flashcard: {} de la colección: {} en workspace: {}", flashcardId, collectionId,
                workspaceId);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        flashcardService.deleteFlashcard(flashcardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{flashcardId}/review")
    @WorkspaceAccess
    public ResponseEntity<Flashcard> submitReview(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("flashcardId") Long flashcardId,
            @RequestBody FlashcardReviewDto reviewDto) {
        log.info("Enviando revisión para flashcard {} en colección {}: {}", flashcardId, collectionId, reviewDto);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId);
        Flashcard flashcard = flashcardService.processReview(flashcardId, reviewDto);
        return ResponseEntity.ok(flashcard);
    }
}
