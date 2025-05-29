package com.example.api_v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api_v2.dto.NoteDto;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.NoteService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/collections")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{collectionId}/notes")
    public ResponseEntity<List<NoteDto>> getNotes(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable(value = "collectionId", required = false) String collectionIdStr) {

        // Validar si el collectionId es válido
        if (collectionIdStr == null || collectionIdStr.equals("undefined")) {
            log.warn("Se intentó acceder a notas con collectionId inválido: {}", collectionIdStr);
            return ResponseEntity.ok(new ArrayList<>()); // Devolver lista vacía
        }

        try {
            Long collectionId = Long.parseLong(collectionIdStr);
            log.info("Obteniendo notas de la colección {} en workspace {}", collectionId, workspaceId);
            return ResponseEntity.ok(noteService.getNotes(collectionId));
        } catch (NumberFormatException e) {
            log.warn("Error al convertir collectionId: {}", collectionIdStr, e);
            return ResponseEntity.ok(new ArrayList<>()); // Devolver lista vacía
        }
    }

    @PostMapping("/{collectionId}/notes/user/{email}")
    @WorkspaceEditAccess
    public ResponseEntity<NoteDto> createNote(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestBody NoteDto note,
            @PathVariable("email") String email) {
        log.info("Creando nota en colección {} del workspace {} por usuario {}: {}", collectionId, workspaceId, email,
                note);
        return ResponseEntity.ok(noteService.createNote(collectionId, note, email));
    }

    @PutMapping("/{collectionId}/notes/{id}")
    @WorkspaceEditAccess
    public ResponseEntity<NoteDto> updateNote(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("id") Long id,
            @RequestBody NoteDto note) {
        log.info("Actualizando nota {} en colección {} del workspace {}: {}", id, collectionId, workspaceId, note);
        return ResponseEntity.ok(noteService.updateNote(collectionId, id, note));
    }

    @GetMapping("/{collectionId}/notes/{id}")
    @WorkspaceAccess
    public ResponseEntity<NoteDto> getNoteById(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("id") Long id) {
        log.info("Obteniendo nota {} de la colección {} en workspace {}", id, collectionId, workspaceId);
        return ResponseEntity.ok(noteService.getNote(id));
    }

    @DeleteMapping("/{collectionId}/notes/{id}")
    @WorkspaceEditAccess
    public ResponseEntity<Void> deleteNote(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("id") Long id) {
        log.info("Eliminando nota {} de la colección {} en workspace {}", id, collectionId, workspaceId);
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
