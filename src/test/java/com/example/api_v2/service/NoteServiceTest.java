package com.example.api_v2.service;

import com.example.api_v2.dto.NoteDto;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.*;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.NoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para NoteService
 * Valida la lógica de negocio relacionada con notas
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private NoteService noteService;

    private User testUser;
    private Workspace testWorkspace;
    private WorkspaceUser testWorkspaceUser;
    private Collection testCollection;
    private Note testNote;
    private NoteDto testNoteDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");
        testWorkspace.setWorkspaceUsers(new ArrayList<>());

        testWorkspaceUser = new WorkspaceUser();
        testWorkspaceUser.setUser(testUser);
        testWorkspaceUser.setWorkspace(testWorkspace);
        testWorkspaceUser.setPermissionType(PermissionType.OWNER);
        testWorkspace.getWorkspaceUsers().add(testWorkspaceUser);

        testCollection = new Collection();
        testCollection.setId(1L);
        testCollection.setName("Test Collection");
        testCollection.setWorkspace(testWorkspace);

        testNote = Note.builder()
                .id(1L)
                .noteName("Test Note")
                .content("Test content for the note")
                .collection(testCollection)
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testNoteDto = NoteDto.builder()
                .noteName("New Note")
                .content("New content for the note")
                .build();
    }

    @Test
    void getNotes_ShouldReturnNoteList_WhenNotesExist() {
        // Given
        List<Note> notes = List.of(testNote);
        when(noteRepository.findByCollectionId(anyLong())).thenReturn(notes);

        // When
        List<NoteDto> result = noteService.getNotes(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        NoteDto noteDto = result.get(0);
        assertEquals(testNote.getNoteName(), noteDto.getNoteName());
        assertEquals(testNote.getContent(), noteDto.getContent());
        assertEquals(testNote.getCollection().getId(), noteDto.getCollectionId());
        
        verify(noteRepository).findByCollectionId(1L);
    }

    @Test
    void getNotes_ShouldReturnEmptyList_WhenNoNotesExist() {
        // Given
        when(noteRepository.findByCollectionId(anyLong())).thenReturn(new ArrayList<>());

        // When
        List<NoteDto> result = noteService.getNotes(1L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(noteRepository).findByCollectionId(1L);
    }

    @Test
    void getNote_ShouldReturnNoteDto_WhenNoteExists() {
        // Given
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));

        // When
        NoteDto result = noteService.getNote(1L);

        // Then
        assertNotNull(result);
        assertEquals(testNote.getId(), result.getId());
        assertEquals(testNote.getNoteName(), result.getNoteName());
        assertEquals(testNote.getContent(), result.getContent());
        assertEquals(testNote.getCollection().getId(), result.getCollectionId());
        assertNotNull(result.getCreatedBy());
        assertEquals(testUser.getId(), result.getCreatedBy().getId());
        
        verify(noteRepository).findById(1L);
    }

    @Test
    void getNote_ShouldThrowException_WhenNoteNotFound() {
        // Given
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> noteService.getNote(999L)
        );
        
        assertEquals("Note not found with id: 999", exception.getMessage());
        verify(noteRepository).findById(999L);
    }

    @Test
    void createNote_ShouldReturnNoteDto_WhenValidInput() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        // When
        NoteDto result = noteService.createNote(1L, testNoteDto, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testNote.getNoteName(), result.getNoteName());
        assertEquals(testNote.getContent(), result.getContent());
        assertEquals(testNote.getCollection().getId(), result.getCollectionId());
        assertNotNull(result.getCreatedBy());
        
        verify(collectionRepository).findById(1L);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void createNote_ShouldThrowException_WhenCollectionNotFound() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> noteService.createNote(999L, testNoteDto, "test@example.com")
        );
        
        assertEquals("Collection not found with id: 999", exception.getMessage());
        verify(collectionRepository).findById(999L);
        verifyNoInteractions(noteRepository);
    }

    @Test
    void createNote_ShouldThrowException_WhenUserNotFoundInWorkspace() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));

        // When & Then
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> noteService.createNote(1L, testNoteDto, "nonexistent@example.com")
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(collectionRepository).findById(1L);
        verifyNoInteractions(noteRepository);
    }

    @Test
    void createNote_ShouldSetCorrectUser_WhenUserFoundInWorkspace() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note savedNote = invocation.getArgument(0);
            savedNote.setId(1L);
            return savedNote;
        });

        // When
        NoteDto result = noteService.createNote(1L, testNoteDto, "test@example.com");

        // Then
        assertNotNull(result);
        verify(noteRepository).save(argThat(note -> 
            note.getCreatedBy() != null && 
            note.getCreatedBy().getEmail().equals("test@example.com")
        ));
    }

    @Test
    void updateNote_ShouldReturnUpdatedNoteDto_WhenNoteExists() {
        // Given
        NoteDto updateDto = NoteDto.builder()
                .noteName("Updated Note")
                .content("Updated content")
                .build();

        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        // When
        NoteDto result = noteService.updateNote(1L, 1L, updateDto);

        // Then
        assertNotNull(result);
        verify(noteRepository).findById(1L);
        verify(noteRepository).save(argThat(note -> 
            note.getUpdatedAt() != null &&
            note.getUpdatedAt().isAfter(testNote.getCreatedAt())
        ));
    }

    @Test
    void updateNote_ShouldThrowException_WhenNoteNotFound() {
        // Given
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> noteService.updateNote(1L, 999L, testNoteDto)
        );
        
        assertEquals("Note not found with id: 999", exception.getMessage());
        verify(noteRepository).findById(999L);
        verify(noteRepository, never()).save(any());
    }

    @Test
    void updateNote_ShouldUpdateCorrectFields_WhenValidInput() {
        // Given
        NoteDto updateDto = NoteDto.builder()
                .noteName("Updated Note Name")
                .content("Updated note content")
                .build();

        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NoteDto result = noteService.updateNote(1L, 1L, updateDto);

        // Then
        assertNotNull(result);
        verify(noteRepository).save(argThat(note -> 
            "Updated Note Name".equals(note.getNoteName()) &&
            "Updated note content".equals(note.getContent()) &&
            note.getUpdatedAt() != null
        ));
    }

    @Test
    void deleteNote_ShouldDeleteNote_WhenNoteExists() {
        // Given
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));

        // When
        noteService.deleteNote(1L);

        // Then
        verify(noteRepository).findById(1L);
        verify(noteRepository).delete(testNote);
    }

    @Test
    void deleteNote_ShouldThrowException_WhenNoteNotFound() {
        // Given
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> noteService.deleteNote(999L)
        );
        
        assertEquals("Note not found with id: 999", exception.getMessage());
        verify(noteRepository).findById(999L);
        verify(noteRepository, never()).delete(any());
    }

    @Test
    void createNote_ShouldHandleNullCreatedBy_WhenConvertingToDto() {
        // Given
        Note noteWithoutCreatedBy = Note.builder()
                .id(1L)
                .noteName("Test Note")
                .content("Test content")
                .collection(testCollection)
                .createdBy(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));
        when(noteRepository.save(any(Note.class))).thenReturn(noteWithoutCreatedBy);

        // When
        NoteDto result = noteService.createNote(1L, testNoteDto, "test@example.com");

        // Then
        assertNotNull(result);
        // El createdBy en el DTO debe ser null cuando la nota no tiene createdBy
        // (aunque esto no debería pasar en la práctica ya que siempre se asigna un usuario)
    }
} 