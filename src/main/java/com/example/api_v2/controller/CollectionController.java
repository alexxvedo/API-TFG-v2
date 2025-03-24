package com.example.api_v2.controller;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.dto.FlashcardStatsDto;
import com.example.api_v2.service.CollectionService;
import com.example.api_v2.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final StatsService statsService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<CollectionDto>> getCollectionsByWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        return ResponseEntity.ok(collectionService.getCollectionsByWorkspace(workspaceId));
    }

    @GetMapping("/workspace/{workspaceId}/{collectionId}")
    public ResponseEntity<CollectionDto> getCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId) {
        return ResponseEntity.ok(collectionService.getCollection(workspaceId, collectionId));
    }

    @PostMapping("/workspace/{workspaceId}/user/{email}")
    public ResponseEntity<CollectionDto> createCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody CollectionDto collectionDto,
            @PathVariable("email") String email) {
        return ResponseEntity.ok(collectionService.createCollection(workspaceId, collectionDto, email));
    }

    @PutMapping("/workspace/{workspaceId}/{collectionId}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestBody CollectionDto collectionDto) {
        return ResponseEntity.ok(collectionService.updateCollection(workspaceId, collectionId, collectionDto));
    }

    @DeleteMapping("/workspace/{workspaceId}/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId) {
        collectionService.deleteCollection(workspaceId, collectionId);
        return ResponseEntity.ok().build();
    }
    
}
