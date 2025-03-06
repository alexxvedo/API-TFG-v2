package com.example.api_v2.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api_v2.dto.NoteDto;
import com.example.api_v2.service.NoteService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/notes")
@CrossOrigin(origins = "*")
public class NoteController {
    

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<List<NoteDto>> getNotes(@PathVariable Long collectionId) {
        return ResponseEntity.ok(noteService.getNotes(collectionId));
    }

    @PostMapping("/{collectionId}/user/{email}")
    public ResponseEntity<NoteDto> createNote(@PathVariable Long collectionId, @RequestBody NoteDto note, @PathVariable String email) {
        return ResponseEntity.ok(noteService.createNote(collectionId, note, email));
    }

    @PutMapping("/{collectionId}/{id}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long collectionId, @PathVariable Long id, @RequestBody NoteDto note) {
        return ResponseEntity.ok(noteService.updateNote(collectionId, id, note));
    }

    @DeleteMapping("/{collectionId}/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long collectionId, @PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }


}
