package com.example.api_v2.controller;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.CollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de colecciones
 * 
 * Las colecciones son contenedores de materiales de estudio dentro de un workspace.
 * Permiten organizar flashcards, documentos y notas de manera estructurada.
 */
@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * Obtiene todas las colecciones de un workspace
     * 
     * @param workspaceId ID del workspace
     * @return Lista de colecciones del workspace
     */
    @GetMapping
    @WorkspaceAccess
    public ResponseEntity<List<CollectionDto>> getCollectionsByWorkspace(
            @PathVariable("workspaceId") Long workspaceId) {
        log.info("Obteniendo colecciones para el workspace: {}", workspaceId);
        
        // Validar el ID del workspace
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        return ResponseEntity.ok(collectionService.getCollectionsByWorkspace(workspaceId));
    }

    /**
     * Obtiene una colección específica con información del usuario
     * 
     * @param workspaceId ID del workspace
     * @param collectionId ID de la colección
     * @param email Email del usuario
     * @return Datos de la colección con información específica del usuario
     */
    @GetMapping("/{collectionId}/user/{email}")
    @WorkspaceAccess
    public ResponseEntity<CollectionDto> getCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("email") String email
    ) {
        log.info("Obteniendo colección {} del workspace {}", collectionId, workspaceId);
        
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
        
        return ResponseEntity.ok(collectionService.getCollection(workspaceId, collectionId, email));
    }

    /**
     * Crea una nueva colección en el workspace
     * 
     * @param workspaceId ID del workspace donde crear la colección
     * @param collectionDto Datos de la nueva colección
     * @param email Email del usuario que crea la colección
     * @return Datos de la colección creada
     */
    @PostMapping("/user/{email}")
    @WorkspaceEditAccess
    public ResponseEntity<CollectionDto> createCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody CollectionDto collectionDto,
            @PathVariable("email") String email) {
        log.info("Creando colección en workspace {} para usuario {}: {}", workspaceId, email, collectionDto);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        // Validar el DTO de la colección
        if (collectionDto == null) {
            log.error("Datos de colección no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la colección son obligatorios");
        }
        
        // Ya que hemos verificado que collectionDto no es nulo, podemos acceder a sus propiedades
        if (collectionDto != null && (collectionDto.getName() == null || collectionDto.getName().trim().isEmpty())) {
            log.error("Nombre de colección no proporcionado");
            ErrorUtils.throwValidationError("El nombre de la colección es obligatorio");
        }
        
        return ResponseEntity.ok(collectionService.createCollection(workspaceId, collectionDto, email));
    }

    /**
     * Actualiza una colección existente
     * 
     * @param workspaceId ID del workspace
     * @param collectionId ID de la colección a actualizar
     * @param collectionDto Nuevos datos de la colección
     * @return Datos de la colección actualizada
     */
    @PutMapping("/{collectionId}")
    @WorkspaceEditAccess
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestBody CollectionDto collectionDto) {
        log.info("Actualizando colección {} en workspace {}: {}", collectionId, workspaceId, collectionDto);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (collectionId == null || collectionId <= 0) {
            log.error("ID de colección inválido: {}", collectionId);
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        // Validar el DTO de la colección
        if (collectionDto == null) {
            log.error("Datos de colección no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la colección son obligatorios");
        }
        
        // Ya que hemos verificado que collectionDto no es nulo, podemos acceder a sus propiedades
        if (collectionDto != null && (collectionDto.getName() == null || collectionDto.getName().trim().isEmpty())) {
            log.error("Nombre de colección no proporcionado");
            ErrorUtils.throwValidationError("El nombre de la colección es obligatorio");
        }
        
        return ResponseEntity.ok(collectionService.updateCollection(workspaceId, collectionId, collectionDto));
    }

    /**
     * Elimina una colección del workspace
     * 
     * @param workspaceId ID del workspace
     * @param collectionId ID de la colección a eliminar
     * @param email Email del usuario que elimina la colección
     * @return Respuesta vacía indicando éxito
     */
    @DeleteMapping("/{collectionId}/user/{email}")
    @WorkspaceEditAccess
    public ResponseEntity<Void> deleteCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("email") String email) {
        log.info("Eliminando colección {} del workspace {} por usuario {}", collectionId, workspaceId, email);
        
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
        
        collectionService.deleteCollection(workspaceId, collectionId, email);
        return ResponseEntity.noContent().build();
    }
}
