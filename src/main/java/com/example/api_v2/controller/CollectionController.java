package com.example.api_v2.controller;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.CollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    @WorkspaceAccess
    public ResponseEntity<List<CollectionDto>> getCollectionsByWorkspace(
            @PathVariable("workspaceId") Long workspaceId) {
        log.info("Obteniendo colecciones para el workspace: {}", workspaceId);
        return ResponseEntity.ok(collectionService.getCollectionsByWorkspace(workspaceId));
    }

    @GetMapping("/{collectionId}/user/{email}")
    @WorkspaceAccess
    public ResponseEntity<CollectionDto> getCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("email") String email
    ) {
        log.info("Obteniendo colección {} del workspace {}", collectionId, workspaceId);
        return ResponseEntity.ok(collectionService.getCollection(workspaceId, collectionId, email));
    }

    @PostMapping("/user/{email}")
    @WorkspaceEditAccess
    public ResponseEntity<CollectionDto> createCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody CollectionDto collectionDto,
            @PathVariable("email") String email) {
        log.info("Creando colección en workspace {} para usuario {}: {}", workspaceId, email, collectionDto);
        return ResponseEntity.ok(collectionService.createCollection(workspaceId, collectionDto, email));
    }

    @PutMapping("/{collectionId}")
    @WorkspaceEditAccess
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestBody CollectionDto collectionDto) {
        log.info("Actualizando colección {} en workspace {}: {}", collectionId, workspaceId, collectionDto);
        return ResponseEntity.ok(collectionService.updateCollection(workspaceId, collectionId, collectionDto));
    }

    @DeleteMapping("/{collectionId}")
    @WorkspaceEditAccess
    public ResponseEntity<Void> deleteCollection(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId) {
        log.info("Eliminando colección {} del workspace {}", collectionId, workspaceId);
        collectionService.deleteCollection(workspaceId, collectionId);
        return ResponseEntity.ok().build();
    }
}
