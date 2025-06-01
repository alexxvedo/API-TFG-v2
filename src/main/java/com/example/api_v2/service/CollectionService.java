package com.example.api_v2.service;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.exception.ErrorCode;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceActivity;
import com.example.api_v2.model.User;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.api_v2.repository.WorkspaceActivityRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CollectionService {

    private static final String COLLECTION_NOT_FOUND = "Collection not found";

    private final CollectionRepository collectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceActivityRepository workspaceActivityRepository;
    private final FlashcardService flashcardService;

    public List<CollectionDto> getCollectionsByWorkspace(Long workspaceId) {
        return collectionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::convertToDto)
                .toList();
    }

    public CollectionDto getCollection(Long workspaceId, Long collectionId, String email) {
        log.debug("Buscando colección con ID: {} en workspace: {}", collectionId, workspaceId);
        
        // Buscar la colección
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> {
                    log.error("Colección no encontrada con ID: {}", collectionId);
                    // Lanzar excepción directamente
                    ErrorUtils.throwResourceNotFound("Colección", "id", collectionId);
                    // Esta línea nunca se ejecuta, pero es necesaria para el compilador
                    return null;
                });

        // Verificar que la colección pertenece al workspace especificado
        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            log.error("La colección {} no pertenece al workspace {}", collectionId, workspaceId);
            ErrorUtils.throwInvalidOperation("La colección no pertenece al workspace especificado");
        }

        // Buscar el usuario
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado con email: {}", email);
                    // Lanzar excepción directamente
                    ErrorUtils.throwResourceNotFound("Usuario", "email", email);
                    // Esta línea nunca se ejecuta, pero es necesaria para el compilador
                    return null;
                });

        log.debug("Colección {} encontrada correctamente", collectionId);
        return convertToDto(collection, user.getId());
    }

    public CollectionDto createCollection(Long workspaceId, CollectionDto collectionDto, String email) {
        log.debug("Creando colección en workspace {} para usuario {}", workspaceId, email);
        
        // Validar datos de entrada
        if (collectionDto.getName() == null || collectionDto.getName().trim().isEmpty()) {
            log.error("Intento de crear colección sin nombre en workspace {}", workspaceId);
            ErrorUtils.throwValidationError("El nombre de la colección es obligatorio");
        }
        
        // Buscar el workspace
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> {
                    log.error("Workspace no encontrado con ID: {}", workspaceId);
                    ErrorUtils.throwResourceNotFound("Workspace", "id", workspaceId);
                    return null;
                });

        // Buscar el usuario
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado con email: {}", email);
                    ErrorUtils.throwResourceNotFound("Usuario", "email", email);
                    return null;
                });
        
        // Verificar si ya existe una colección con el mismo nombre en este workspace
        boolean existsWithSameName = collectionRepository.findByWorkspaceIdAndName(workspaceId, collectionDto.getName()).isPresent();
        if (existsWithSameName) {
            log.error("Ya existe una colección con nombre '{}' en el workspace {}", collectionDto.getName(), workspaceId);
            ErrorUtils.throwDuplicateResource("Colección", "nombre", collectionDto.getName());
        }

        // Crear la colección
        Collection collection = new Collection();
        collection.setName(collectionDto.getName());
        collection.setDescription(collectionDto.getDescription());
        collection.setWorkspace(workspace);
        collection.setCreatedBy(user);
        
        // Añadir tags si existen
        if (collectionDto.getTags() != null && !collectionDto.getTags().isEmpty()) {
            collection.setTags(collectionDto.getTags());
        }
        
        // Añadir color si existe
        if (collectionDto.getColor() != null && !collectionDto.getColor().isEmpty()) {
            collection.setColor(collectionDto.getColor());
        }
        
        try {
            collection = collectionRepository.save(collection);
            
            // Registrar la actividad
            WorkspaceActivity activity = new WorkspaceActivity();
            activity.setWorkspace(workspace);
            activity.setUser(user);
            activity.setAction("Created collection: " + collectionDto.getName());
            workspaceActivityRepository.save(activity);
            
            log.info("Colección creada correctamente: {} en workspace {}", collection.getId(), workspaceId);
            return convertToDto(collection);
        } catch (Exception e) {
            log.error("Error al guardar la colección: {}", e.getMessage(), e);
            ErrorUtils.throwOperationFailed("Error al crear la colección: " + e.getMessage());
            return null; // Esta línea nunca se ejecuta
        }
    }

    public CollectionDto updateCollection(Long workspaceId, Long collectionId, CollectionDto collectionDto) {
        log.debug("Actualizando colección {} en workspace {}", collectionId, workspaceId);
        
        // Validar datos de entrada
        if (collectionDto.getName() == null || collectionDto.getName().trim().isEmpty()) {
            log.error("Intento de actualizar colección sin nombre: {}", collectionId);
            ErrorUtils.throwValidationError("El nombre de la colección es obligatorio");
        }
        
        // Buscar la colección
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> {
                    log.error("Colección no encontrada con ID: {}", collectionId);
                    ErrorUtils.throwResourceNotFound("Colección", "id", collectionId);
                    return null;
                });

        // Verificar que la colección pertenece al workspace especificado
        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            log.error("La colección {} no pertenece al workspace {}", collectionId, workspaceId);
            ErrorUtils.throwInvalidOperation("La colección no pertenece al workspace especificado");
        }
        
        // Verificar si ya existe otra colección con el mismo nombre en este workspace (excluyendo esta misma)
        Optional<Collection> existingWithSameName = collectionRepository.findByWorkspaceIdAndName(workspaceId, collectionDto.getName());
        if (existingWithSameName.isPresent() && !existingWithSameName.get().getId().equals(collectionId)) {
            log.error("Ya existe otra colección con nombre '{}' en el workspace {}", collectionDto.getName(), workspaceId);
            ErrorUtils.throwDuplicateResource("Colección", "nombre", collectionDto.getName());
        }

        // Actualizar la colección
        collection.setName(collectionDto.getName());
        collection.setDescription(collectionDto.getDescription());
        
        // Actualizar tags si existen
        if (collectionDto.getTags() != null) {
            collection.setTags(collectionDto.getTags());
        }
        
        // Actualizar color si existe
        if (collectionDto.getColor() != null) {
            collection.setColor(collectionDto.getColor());
        }

        try {
            collection = collectionRepository.save(collection);
            log.info("Colección {} actualizada correctamente", collectionId);
            return convertToDto(collection);
        } catch (Exception e) {
            log.error("Error al actualizar la colección {}: {}", collectionId, e.getMessage(), e);
            ErrorUtils.throwOperationFailed("Error al actualizar la colección: " + e.getMessage());
            return null; // Esta línea nunca se ejecuta
        }
    }

    public void deleteCollection(Long workspaceId, Long collectionId) {
        log.debug("Eliminando colección {} del workspace {}", collectionId, workspaceId);
        
        // Buscar la colección
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> {
                    log.error("Colección no encontrada con ID: {}", collectionId);
                    ErrorUtils.throwResourceNotFound("Colección", "id", collectionId);
                    return null;
                });

        // Verificar que la colección pertenece al workspace especificado
        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            log.error("La colección {} no pertenece al workspace {}", collectionId, workspaceId);
            ErrorUtils.throwInvalidOperation("La colección no pertenece al workspace especificado");
        }
        
        // Verificar si la colección tiene flashcards asociadas
        if (!collection.getFlashcards().isEmpty()) {
            log.warn("Eliminando colección {} que contiene {} flashcards", collectionId, collection.getFlashcards().size());
        }

        try {
            collectionRepository.delete(collection);
            log.info("Colección {} eliminada correctamente", collectionId);
        } catch (Exception e) {
            log.error("Error al eliminar la colección {}: {}", collectionId, e.getMessage(), e);
            ErrorUtils.throwOperationFailed("Error al eliminar la colección: " + e.getMessage());
        }
    }

    private CollectionDto convertToDto(Collection collection) {
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setName(collection.getName());
        dto.setDescription(collection.getDescription());
        dto.setWorkspaceId(collection.getWorkspace().getId());
        dto.setFlashcards(collection.getFlashcards().stream().map(Flashcard::toDto).toList());
        dto.setItemCount(collection.getFlashcards().size());
        dto.setCreatedBy(collection.getCreatedBy().toDto());
        dto.setCreatedAt(collection.getCreatedAt());
        dto.setTags(collection.getTags());
        dto.setColor(collection.getColor());
        return dto;
    }

    private CollectionDto convertToDto(Collection collection, String userId) {
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setName(collection.getName());
        dto.setDescription(collection.getDescription());
        dto.setWorkspaceId(collection.getWorkspace().getId());
        dto.setFlashcards(collection.getFlashcards().stream().map((flashcard) -> flashcardService.convertToDto(flashcard, userId)).toList());
        dto.setItemCount(collection.getFlashcards().size());
        dto.setCreatedBy(collection.getCreatedBy().toDto());
        dto.setCreatedAt(collection.getCreatedAt());
        dto.setTags(collection.getTags());
        dto.setColor(collection.getColor());
        return dto;
    }


}