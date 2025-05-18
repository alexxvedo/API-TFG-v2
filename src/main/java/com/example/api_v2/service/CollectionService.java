package com.example.api_v2.service;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceActivity;
import com.example.api_v2.model.User;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.api_v2.repository.WorkspaceActivityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionService {

    private static final String COLLECTION_NOT_FOUND = "Collection not found";

    private final CollectionRepository collectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceActivityRepository workspaceActivityRepository;

    public List<CollectionDto> getCollectionsByWorkspace(Long workspaceId) {
        return collectionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::convertToDto)
                .toList();
    }

    public CollectionDto getCollection(Long workspaceId, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException(COLLECTION_NOT_FOUND));

        // Verificar que la colección pertenece al workspace especificado
        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            throw new SecurityException("La colección no pertenece al workspace especificado");
        }

        return convertToDto(collection);
    }

    public CollectionDto createCollection(Long workspaceId, CollectionDto collectionDto, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        User user = userRepository.findByEmail(email).orElse(null);

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
        
        collection = collectionRepository.save(collection);

        WorkspaceActivity activity = new WorkspaceActivity();
        activity.setWorkspace(workspace);
        activity.setUser(user);
        activity.setAction("Created collection: " + collectionDto.getName());
        workspaceActivityRepository.save(activity);
        return convertToDto(collection);
    }

    public CollectionDto updateCollection(Long workspaceId, Long collectionId, CollectionDto collectionDto) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException(COLLECTION_NOT_FOUND));

        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException("Collection does not belong to the specified workspace");
        }

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

        collection = collectionRepository.save(collection);
        return convertToDto(collection);
    }

    public void deleteCollection(Long workspaceId, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException(COLLECTION_NOT_FOUND));

        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException("Collection does not belong to the specified workspace");
        }

        collectionRepository.delete(collection);
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
}