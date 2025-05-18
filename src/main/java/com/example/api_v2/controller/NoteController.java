package com.example.api_v2.controller;

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
@RequestMapping("/notes")
public class NoteController {
    
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{collectionId}")
    @WorkspaceAccess
    public ResponseEntity<List<NoteDto>> getNotes(@PathVariable("collectionId") Long collectionId) {
        log.info("Obteniendo notas para la colección: {}", collectionId);
        return ResponseEntity.ok(noteService.getNotes(collectionId));
    }

    @PostMapping("/{collectionId}/user/{email}")
    @WorkspaceEditAccess
    public ResponseEntity<NoteDto> createNote(@PathVariable("collectionId") Long collectionId, @RequestBody NoteDto note, @PathVariable("email") String email) {
        log.info("Creando nota en colección {} por usuario {}: {}", collectionId, email, note);
        return ResponseEntity.ok(noteService.createNote(collectionId, note, email));
    }

    @PutMapping("/{collectionId}/{id}")
    @WorkspaceEditAccess
    public ResponseEntity<NoteDto> updateNote(@PathVariable("collectionId") Long collectionId, @PathVariable("id") Long id, @RequestBody NoteDto note) {
        log.info("Actualizando nota {} en colección {}: {}", id, collectionId, note);
        return ResponseEntity.ok(noteService.updateNote(collectionId, id, note));
    }

    @DeleteMapping("/{collectionId}/{id}")
    @WorkspaceEditAccess
    public ResponseEntity<Void> deleteNote(@PathVariable("collectionId") Long collectionId, @PathVariable("id") Long id) {
        log.info("Eliminando nota {} de la colección {}", id, collectionId);
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
