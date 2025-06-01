package com.example.api_v2.controller;

import com.example.api_v2.dto.FlashcardDto;
import com.example.api_v2.dto.FlashcardReviewDto;
import com.example.api_v2.dto.FlashcardStatsDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.CollectionService;
import com.example.api_v2.service.FlashcardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/collections/{collectionId}/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final CollectionService collectionService;

    public FlashcardController(FlashcardService flashcardService, CollectionService collectionService) {
        this.flashcardService = flashcardService;
        this.collectionService = collectionService;
    }

    @GetMapping
    @WorkspaceAccess
    public ResponseEntity<List<FlashcardDto>> getFlashcardsByUserCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestParam(value = "email", required = false) String email) {
        log.info("Obteniendo flashcards de la colección {} en workspace {} para usuario {}", collectionId, workspaceId, email);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        // Verificar que la colección pertenece al workspace
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
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        // Spring Security garantiza que principal no es nulo cuando se usa @WorkspaceAccess,
        // pero agregamos esta verificación por seguridad
        if (principal == null) {
            log.error("Usuario no autenticado");
            ErrorUtils.throwInsufficientPermissions("Usuario no autenticado");
        }
        
        // Verificar que la colección pertenece al workspace
        // Usamos getName() solo después de verificar que principal no es nulo
        String userId = principal != null ? principal.getName() : null;
        collectionService.getCollection(workspaceId, collectionId, userId);
        
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
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
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
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        // Validar el DTO de la flashcard
        if (flashcardDto == null) {
            log.error("Datos de flashcard no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la flashcard son obligatorios");
        }
        
        // Ya que hemos verificado que flashcardDto no es nulo, podemos acceder a sus propiedades
        if (flashcardDto != null) {
            if (flashcardDto.getQuestion() == null || flashcardDto.getQuestion().trim().isEmpty()) {
                log.error("Pregunta de flashcard no proporcionada");
                ErrorUtils.throwValidationError("La pregunta de la flashcard es obligatoria");
            }
            
            if (flashcardDto.getAnswer() == null || flashcardDto.getAnswer().trim().isEmpty()) {
                log.error("Respuesta de flashcard no proporcionada");
                ErrorUtils.throwValidationError("La respuesta de la flashcard es obligatoria");
            }
        }
        
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
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        if (flashcardId == null || flashcardId <= 0) {
            log.error("ID de flashcard inválido: {}", flashcardId);
            ErrorUtils.throwValidationError("El ID de la flashcard debe ser un número positivo");
        }
        
        // Validar el DTO de la flashcard
        if (flashcardDto == null) {
            log.error("Datos de flashcard no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la flashcard son obligatorios");
        }
        
        // Ya que hemos verificado que flashcardDto no es nulo, podemos acceder a sus propiedades
        if (flashcardDto != null) {
            if (flashcardDto.getQuestion() == null || flashcardDto.getQuestion().trim().isEmpty()) {
                log.error("Pregunta de flashcard no proporcionada");
                ErrorUtils.throwValidationError("La pregunta de la flashcard es obligatoria");
            }
            
            if (flashcardDto.getAnswer() == null || flashcardDto.getAnswer().trim().isEmpty()) {
                log.error("Respuesta de flashcard no proporcionada");
                ErrorUtils.throwValidationError("La respuesta de la flashcard es obligatoria");
            }
        }
        
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
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        if (flashcardId == null || flashcardId <= 0) {
            log.error("ID de flashcard inválido: {}", flashcardId);
            ErrorUtils.throwValidationError("El ID de la flashcard debe ser un número positivo");
        }
        
        // Verificar que la colección pertenece al workspace y que la flashcard existe
        flashcardService.deleteFlashcard(flashcardId);
        return ResponseEntity.noContent().build();
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
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        if (flashcardId == null || flashcardId <= 0) {
            log.error("ID de flashcard inválido: {}", flashcardId);
            ErrorUtils.throwValidationError("El ID de la flashcard debe ser un número positivo");
        }
        
        if (requestBody == null || requestBody.isEmpty()) {
            log.error("Datos de revisión no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la revisión son obligatorios");
        }
        
        // Spring Security garantiza que principal no es nulo cuando se usa @WorkspaceAccess,
        // pero agregamos esta verificación por seguridad
        if (principal == null) {
            log.error("Usuario no autenticado");
            ErrorUtils.throwInsufficientPermissions("Usuario no autenticado");
        }
        
        // Verificar que la colección pertenece al workspace
        collectionService.getCollection(workspaceId, collectionId, email);
        
        // Crear un DTO con los campos mapeados correctamente
        FlashcardReviewDto reviewDto = new FlashcardReviewDto();
        
        // Mapear reviewResult a result
        if (requestBody != null && requestBody.containsKey("reviewResult")) {
            reviewDto.setResult((String) requestBody.get("reviewResult"));
            log.info("Mapeando reviewResult: {} a result", requestBody.get("reviewResult"));
        } else {
            log.error("Resultado de la revisión no proporcionado");
            ErrorUtils.throwValidationError("El resultado de la revisión es obligatorio");
        }
        
        // Mapear timeSpentMs
        if (requestBody != null && requestBody.containsKey("timeSpentMs")) {
            Object timeObj = requestBody.get("timeSpentMs");
            if (timeObj instanceof Integer) {
                reviewDto.setTimeSpentMs(((Integer) timeObj).longValue());
            } else if (timeObj instanceof Long) {
                reviewDto.setTimeSpentMs((Long) timeObj);
            } else if (timeObj instanceof Number) {
                reviewDto.setTimeSpentMs(((Number) timeObj).longValue());
            } else {
                log.error("Formato de tiempo de estudio inválido");
                ErrorUtils.throwValidationError("El formato del tiempo de estudio es inválido");
            }
            log.info("Tiempo de estudio en ms: {}", reviewDto.getTimeSpentMs());
        } else {
            log.error("Tiempo de estudio no proporcionado");
            ErrorUtils.throwValidationError("El tiempo de estudio es obligatorio");
        }
        
        // Obtener el ID del usuario
        String userId;
        if (requestBody != null && requestBody.containsKey("userId")) {
            userId = (String) requestBody.get("userId");
            if (userId == null || userId.trim().isEmpty()) {
                log.error("ID de usuario inválido");
                ErrorUtils.throwValidationError("El ID del usuario es inválido");
            }
            reviewDto.setUserId(userId);
            log.info("Usando ID de usuario enviado desde el frontend: {}", userId);
        } else {
            // Fallback al ID del usuario autenticado
            userId = principal != null ? principal.getName() : null;
            if (userId == null || userId.trim().isEmpty()) {
                log.error("No se pudo obtener el ID del usuario");
                ErrorUtils.throwValidationError("No se pudo obtener el ID del usuario");
            }
            reviewDto.setUserId(userId);
            log.info("Usando ID de usuario de la autenticación: {}", userId);
        }
        
        Flashcard flashcard = flashcardService.processReview(flashcardId, reviewDto, userId);
        return ResponseEntity.ok(flashcard);
    }
}
