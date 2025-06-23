package com.example.api_v2.service;

import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.NoteRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.api_v2.model.Collection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// No necesitamos Hibernate para este servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api_v2.dto.NoteDto;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.Note;
import com.example.api_v2.model.User;
import com.example.api_v2.model.WorkspaceUser;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final CollectionRepository collectionRepository;
    private final WorkspaceActivityService workspaceActivityService;

    @Autowired
    public NoteService(NoteRepository noteRepository, CollectionRepository collectionRepository, WorkspaceActivityService workspaceActivityService) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
        this.workspaceActivityService = workspaceActivityService;
    }

    public List<NoteDto> getNotes(Long collectionId) {
        return noteRepository.findByCollectionId(collectionId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public NoteDto getNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
        return convertToDto(note);
    }

    public NoteDto createNote(Long collectionId, NoteDto noteDto, String email) {

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        // Obtenemos el usuario del workspace que creo la Flashcard
        User user = collection.getWorkspace().getWorkspaceUsers().stream()
                .map(WorkspaceUser::getUser)
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Note note = Note.builder()
                .noteName(noteDto.getNoteName())
                .content(noteDto.getContent())
                .collection(collection)
                .createdBy(user)
                .build();

        note = noteRepository.save(note);

        // Registrar la actividad
        workspaceActivityService.logNoteCreated(
            collection.getWorkspace().getId(), 
            email, 
            noteDto.getNoteName(), 
            collection.getName()
        );

        // No es necesario forzar la inicialización

        return convertToDto(note);
    }

    public NoteDto updateNote(Long collectionId, Long id, NoteDto noteDto) {

        System.out.println(noteDto.toString());

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        updateNoteFromDto(noteDto, note);
        note.setUpdatedAt(LocalDateTime.now());

        return convertToDto(noteRepository.save(note));

    }

    public void deleteNote(Long id, String userEmail) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        // Registrar la actividad antes de eliminar
        workspaceActivityService.logNoteDeleted(
            note.getCollection().getWorkspace().getId(), 
            userEmail, 
            note.getNoteName(), 
            note.getCollection().getName()
        );

        noteRepository.delete(note);
    }

    private NoteDto convertToDto(Note note) {
        // No es necesario forzar la inicialización

        return NoteDto.builder()
                .id(note.getId())
                .collectionId(note.getCollection().getId())
                .noteName(note.getNoteName())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .createdBy(note.getCreatedBy() != null ? note.getCreatedBy().toDto() : null)
                .build();
    }

    private void updateNoteFromDto(NoteDto noteDto, Note note) {
        note.setNoteName(noteDto.getNoteName());
        note.setContent(noteDto.getContent());
        note.setUpdatedAt(LocalDateTime.now());
    }
}
