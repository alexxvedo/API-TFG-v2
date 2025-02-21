package com.example.api_v2.service;

import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.NoteRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import com.example.api_v2.model.Collection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api_v2.dto.NoteDto;
import com.example.api_v2.dto.WorkspaceUserDto;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.Note;
import com.example.api_v2.model.User;
import com.example.api_v2.model.WorkspaceUser;

@Service
@RequiredArgsConstructor
public class NoteService {
    

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private CollectionRepository collectionRepository;


    public NoteService(NoteRepository noteRepository, CollectionRepository collectionRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
    }

    public List<NoteDto> getNotes(Long collectionId) {
        return noteRepository.findByCollectionId(collectionId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public NoteDto createNote(Long collectionId, NoteDto noteDto, String userId) {

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        // Obtenemos el usuario del workspace que creo la Flashcard
        User user = collection.getWorkspace().getWorkspaceUsers().stream()
                .map(WorkspaceUser::getUser)
                .filter(u -> u.getId().equals(userId))  
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        Note note = Note.builder()
                .noteName(noteDto.getNoteName())
                .content(noteDto.getContent())
                .collection(collection)
                .createdBy(user)
                .build();

        note = noteRepository.save(note);

        // Forzar la inicialización del objeto para evitar que sea un proxy
        Hibernate.initialize(note.getCreatedBy());

        return convertToDto(note);
    }

    public NoteDto updateNote(Long collectionId, Long id, NoteDto noteDto) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        updateNoteFromDto(noteDto, note);
        note.setUpdatedAt(LocalDateTime.now());

        return convertToDto(noteRepository.save(note));

    }

    public void deleteNote(Long id) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        noteRepository.delete(note);
    }


    private NoteDto convertToDto(Note note) {

        Hibernate.initialize(note.getCreatedBy());  // Forzar carga de createdBy
        


        return new NoteDto(
                note.getId(),
                note.getCollection().getId(),
                note.getNoteName(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getCreatedBy()
        );
    }

    private void updateNoteFromDto(NoteDto noteDto, Note note) {
        note.setNoteName(noteDto.getNoteName());
        note.setContent(noteDto.getContent());
        note.setUpdatedAt(LocalDateTime.now());
    }
}
