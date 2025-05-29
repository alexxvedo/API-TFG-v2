package com.example.api_v2.controller;

import com.example.api_v2.dto.FlashcardDto;
import com.example.api_v2.dto.FlashcardGenerationDto;
import com.example.api_v2.dto.FlashcardReviewDto;
import com.example.api_v2.dto.FlashcardStatsDto;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.model.User;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.AIService;
import com.example.api_v2.service.CollectionService;
import com.example.api_v2.service.FlashcardService;
import com.example.api_v2.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/collections/{collectionId}/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final AIService aiService;
    private final CollectionService collectionService;
    private final UserService userService;
    private final UserRepository userRepository;

    public FlashcardController(FlashcardService flashcardService, AIService aiService,
                               CollectionService collectionService, UserService userService, UserRepository userRepository) {
        this.flashcardService = flashcardService;
        this.aiService = aiService;
        this.collectionService = collectionService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @WorkspaceAccess
    public ResponseEntity<List<FlashcardDto>> getFlashcardsByUserCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestParam(value = "email", required = false) String email) {

        collectionService.getCollection(workspaceId, collectionId, email);

        return ResponseEntity.ok(flashcardService.getFlashcardsByCollectionWithProgress(collectionId, email));
    }

    @GetMapping("/review")
    @WorkspaceAccess
    public ResponseEntity<List<Flashcard>> getFlashcardsForReview(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            Principal principal) {
        log.info("Obteniendo flashcards para revisión de la colección: {} en workspace: {}", collectionId, workspaceId);
        // Obtener el ID del usuario actual
        String userId = principal.getName();
        List<Flashcard> flashcards = flashcardService.getFlashcardsForReview(collectionId, userId);
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/stats/user/{email}")
    @WorkspaceAccess
    public ResponseEntity<FlashcardStatsDto> getFlashcardStats(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("email") String email) {
        log.info("Obteniendo estadísticas de flashcards para la colección: {} en workspace: {} para el usuario: {}", collectionId,
                workspaceId, email);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId, email);
        return ResponseEntity.ok(flashcardService.getFlashcardStats(collectionId, email));
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
        collectionService.getCollection(workspaceId, collectionId, email);
        return ResponseEntity.ok(flashcardService.createFlashcard(collectionId, flashcardDto, email));
    }



    @PutMapping("/{flashcardId}")
    @WorkspaceEditAccess
    public ResponseEntity<FlashcardDto> updateFlashcard(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("flashcardId") Long flashcardId,
            @RequestBody FlashcardDto flashcardDto,
            @RequestParam(value = "email", required = false) String email

    ) {
        log.info("Actualizando flashcard {} en colección {}: {}", flashcardId, collectionId, flashcardDto);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId, email);
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
        flashcardService.deleteFlashcard(flashcardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{flashcardId}/review")
    @WorkspaceAccess
    public ResponseEntity<Flashcard> submitReview(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("flashcardId") Long flashcardId,
            @RequestBody Map<String, Object> requestBody,
            @RequestParam(value = "email", required = false) String email,
            Principal principal) {
        log.info("Enviando revisión para flashcard {} en colección {}: {}", flashcardId, collectionId, requestBody);
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId, email);
        
        // Crear un DTO con los campos mapeados correctamente
        FlashcardReviewDto reviewDto = new FlashcardReviewDto();
        
        // Mapear reviewResult a result
        if (requestBody.containsKey("reviewResult")) {
            reviewDto.setResult((String) requestBody.get("reviewResult"));
            log.info("Mapeando reviewResult: {} a result", requestBody.get("reviewResult"));
        }
        
        // Mapear timeSpentMs
        if (requestBody.containsKey("timeSpentMs")) {
            Object timeObj = requestBody.get("timeSpentMs");
            if (timeObj instanceof Integer) {
                reviewDto.setTimeSpentMs(((Integer) timeObj).longValue());
            } else if (timeObj instanceof Long) {
                reviewDto.setTimeSpentMs((Long) timeObj);
            } else if (timeObj instanceof Number) {
                reviewDto.setTimeSpentMs(((Number) timeObj).longValue());
            }
            log.info("Tiempo de estudio en ms: {}", reviewDto.getTimeSpentMs());
        }
        
        // Obtener el ID del usuario
        String userId;
        if (requestBody.containsKey("userId")) {
            userId = (String) requestBody.get("userId");
            reviewDto.setUserId(userId);
            log.info("Usando ID de usuario enviado desde el frontend: {}", userId);
        } else {
            // Fallback al ID del usuario autenticado
            userId = principal.getName();
            reviewDto.setUserId(userId);
            log.info("Usando ID de usuario de la autenticación: {}", userId);
        }
        
        Flashcard flashcard = flashcardService.processReview(flashcardId, reviewDto, userId);
        return ResponseEntity.ok(flashcard);
    }
}
