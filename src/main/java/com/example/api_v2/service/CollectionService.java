package com.example.api_v2.service;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.FlashcardRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FlashcardRepository flashcardRepository;

    public List<CollectionDto> getCollectionsByWorkspace(Long workspaceId) {
        return collectionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CollectionDto getCollection(Long workspaceId, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException("Collection does not belong to the specified workspace");
        }

        return convertToDto(collection);
    }

    public CollectionDto createCollection(Long workspaceId, CollectionDto collectionDto, String userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        Collection collection = new Collection();
        collection.setName(collectionDto.getName());
        collection.setDescription(collectionDto.getDescription());
        collection.setWorkspace(workspace);
        collection.setCreatedBy(workspace.getUsers().stream()
                .filter(user -> user.getUser().getClerkId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found")));
        collection = collectionRepository.save(collection);
        return convertToDto(collection);
    }

    public CollectionDto updateCollection(Long workspaceId, Long collectionId, CollectionDto collectionDto) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        if (!collection.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException("Collection does not belong to the specified workspace");
        }

        collection.setName(collectionDto.getName());
        collection.setDescription(collectionDto.getDescription());

        collection = collectionRepository.save(collection);
        return convertToDto(collection);
    }

    public void deleteCollection(Long workspaceId, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

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
        dto.setFlashcards(collection.getFlashcards());
        dto.setItemCount(collection.getFlashcards().size());
        return dto;
    }
}