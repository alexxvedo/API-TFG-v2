package com.example.api_v2.controller;


import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<CollectionDto>> getCollectionsByWorkspace(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(collectionService.getCollectionsByWorkspace(workspaceId));
    }

    @GetMapping("/workspace/{workspaceId}/{collectionId}")
    public ResponseEntity<CollectionDto> getCollection(
            @PathVariable Long workspaceId,
            @PathVariable Long collectionId) {
        return ResponseEntity.ok(collectionService.getCollection(workspaceId, collectionId));
    }

    @PostMapping("/workspace/{workspaceId}/user/{userId}")
    public ResponseEntity<CollectionDto> createCollection(
            @PathVariable Long workspaceId,
            @RequestBody CollectionDto collectionDto,
            @PathVariable String userId) {
        return ResponseEntity.ok(collectionService.createCollection(workspaceId, collectionDto, userId));
    }

    @PutMapping("/workspace/{workspaceId}/{collectionId}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long workspaceId,
            @PathVariable Long collectionId,
            @RequestBody CollectionDto collectionDto) {
        return ResponseEntity.ok(collectionService.updateCollection(workspaceId, collectionId, collectionDto));
    }

    @DeleteMapping("/workspace/{workspaceId}/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable Long workspaceId,
            @PathVariable Long collectionId) {
        collectionService.deleteCollection(workspaceId, collectionId);
        return ResponseEntity.ok().build();
    }
}

