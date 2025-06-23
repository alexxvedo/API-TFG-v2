package com.example.api_v2.service;

import com.example.api_v2.dto.FlashcardDto;
import com.example.api_v2.dto.FlashcardReviewDto;
import com.example.api_v2.dto.FlashcardStatsDto;
import com.example.api_v2.dto.UserDto;
import com.example.api_v2.dto.UserStatsDto;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.*;
import com.example.api_v2.model.Collection;
import com.example.api_v2.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import org.hibernate.Hibernate;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FlashcardService
 * Valida la lógica de negocio relacionada con flashcards y el sistema de repaso espaciado
 */
@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private FlashcardReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFlashcardProgressRepository userFlashcardProgressRepository;

    @Mock
    private UserStatsService userStatsService;

    @InjectMocks
    private FlashcardService flashcardService;

    private User testUser;
    private Workspace testWorkspace;
    private Collection testCollection;
    private Flashcard testFlashcard;
    private UserFlashcardProgress testProgress;
    private FlashcardDto testFlashcardDto;

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

        WorkspaceUser workspaceUser = new WorkspaceUser();
        workspaceUser.setUser(testUser);
        workspaceUser.setWorkspace(testWorkspace);
        workspaceUser.setPermissionType(PermissionType.OWNER);
        testWorkspace.getWorkspaceUsers().add(workspaceUser);

        testCollection = new Collection();
        testCollection.setId(1L);
        testCollection.setName("Test Collection");
        testCollection.setWorkspace(testWorkspace);
        
        // Asegurar que el workspace tenga usuarios para el test de createFlashcard
        testWorkspace.setWorkspaceUsers(List.of(workspaceUser));

        // Crear primero el progreso sin la flashcard para evitar dependencia circular
        testProgress = new UserFlashcardProgress();
        testProgress.setId(1L);
        testProgress.setUser(testUser);
        testProgress.setCollection(testCollection);
        testProgress.setKnowledgeLevel(KnowledgeLevel.BIEN);
        testProgress.setRepetitionLevel(1);
        testProgress.setEaseFactor(2.5);
        testProgress.setNextReviewDate(LocalDateTime.now().plusDays(1));
        testProgress.setLastReviewedAt(LocalDateTime.now());
        testProgress.setReviewCount(5);
        testProgress.setSuccessCount(3);
        testProgress.setFailureCount(2);
        testProgress.setStudyTimeInSeconds(120);

        testFlashcard = new Flashcard();
        testFlashcard.setId(1L);
        testFlashcard.setQuestion("Test Question");
        testFlashcard.setAnswer("Test Answer");
        testFlashcard.setCollection(testCollection);
        testFlashcard.setCreatedBy(testUser);
        testFlashcard.setCreatedAt(LocalDateTime.now());
        testFlashcard.setUpdatedAt(LocalDateTime.now());
        
        // Configurar la relación bidireccional
        testProgress.setFlashcard(testFlashcard);
        testFlashcard.setUserFlashcardProgress(List.of(testProgress));

        testFlashcardDto = new FlashcardDto();
        testFlashcardDto.setQuestion("New Question");
        testFlashcardDto.setAnswer("New Answer");
        
        // Configurar createdBy para evitar NullPointerException en updateFlashcard
        UserDto testUserDto = new UserDto();
        testUserDto.setId("test-user-id");
        testUserDto.setEmail("test@example.com");
        testUserDto.setName("Test User");
        testFlashcardDto.setCreatedBy(testUserDto);
    }

    @Test
    void getFlashcardsByCollectionWithProgress_ShouldReturnFlashcardsWithProgress_WhenUserHasProgress() {
        // Given
        List<Flashcard> flashcards = List.of(testFlashcard);
        List<UserFlashcardProgress> progresses = List.of(testProgress);
        
        when(flashcardRepository.findByCollectionId(anyLong())).thenReturn(flashcards);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userFlashcardProgressRepository.findByCollectionIdAndUserId(anyLong(), anyString())).thenReturn(progresses);

        // When
        List<FlashcardDto> result = flashcardService.getFlashcardsByCollectionWithProgress(1L, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        FlashcardDto dto = result.get(0);
        assertEquals(testFlashcard.getQuestion(), dto.getQuestion());
        assertEquals(testFlashcard.getAnswer(), dto.getAnswer());
        assertEquals(KnowledgeLevel.BIEN, dto.getKnowledgeLevel());
        assertEquals(5, dto.getReviewCount());
        assertEquals("completada", dto.getStatus());
        
        verify(flashcardRepository).findByCollectionId(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(userFlashcardProgressRepository).findByCollectionIdAndUserId(1L, "test-user-id");
    }

    @Test
    void getFlashcardsByCollectionWithProgress_ShouldReturnFlashcardsWithoutProgress_WhenUserHasNoProgress() {
        // Given
        List<Flashcard> flashcards = List.of(testFlashcard);
        
        when(flashcardRepository.findByCollectionId(anyLong())).thenReturn(flashcards);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userFlashcardProgressRepository.findByCollectionIdAndUserId(anyLong(), anyString())).thenReturn(new ArrayList<>());

        // When
        List<FlashcardDto> result = flashcardService.getFlashcardsByCollectionWithProgress(1L, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        FlashcardDto dto = result.get(0);
        assertEquals(testFlashcard.getQuestion(), dto.getQuestion());
        assertEquals(testFlashcard.getAnswer(), dto.getAnswer());
        assertEquals("sinHacer", dto.getStatus());
        assertNull(dto.getKnowledgeLevel());
        
        verify(flashcardRepository).findByCollectionId(1L);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getFlashcardsForReview_ShouldReturnPrioritizedFlashcards_WhenFlashcardsExist() {
        // Given
        List<Flashcard> flashcards = List.of(testFlashcard);
        
        when(flashcardRepository.findByCollectionId(anyLong())).thenReturn(flashcards);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(userFlashcardProgressRepository.findByFlashcardIdAndUserId(anyLong(), anyString())).thenReturn(Optional.of(testProgress));

        // When
        List<Flashcard> result = flashcardService.getFlashcardsForReview(1L, "test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFlashcard.getId(), result.get(0).getId());
        
        verify(flashcardRepository).findByCollectionId(1L);
        verify(userRepository).findById("test-user-id");
        verify(userFlashcardProgressRepository).findByFlashcardIdAndUserId(1L, "test-user-id");
    }

    @Test
    void getFlashcardsForReview_ShouldCreateProgressForNewFlashcards_WhenNoProgressExists() {
        // Given
        List<Flashcard> flashcards = List.of(testFlashcard);
        
        when(flashcardRepository.findByCollectionId(anyLong())).thenReturn(flashcards);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(userFlashcardProgressRepository.findByFlashcardIdAndUserId(anyLong(), anyString())).thenReturn(Optional.empty());
        when(userFlashcardProgressRepository.save(any(UserFlashcardProgress.class))).thenReturn(testProgress);

        // When
        List<Flashcard> result = flashcardService.getFlashcardsForReview(1L, "test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(userFlashcardProgressRepository).save(any(UserFlashcardProgress.class));
    }

    @Test
    void getFlashcardsForReview_ShouldThrowException_WhenUserNotFound() {
        // Given
        List<Flashcard> flashcards = List.of(testFlashcard);
        
        when(flashcardRepository.findByCollectionId(anyLong())).thenReturn(flashcards);
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> flashcardService.getFlashcardsForReview(1L, "nonexistent-user")
        );
        
        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(flashcardRepository).findByCollectionId(1L);
        verify(userRepository).findById("nonexistent-user");
    }

    @Test
    void getFlashcardStats_ShouldReturnStats_WhenUserExists() {
        // Given
        List<UserFlashcardProgress> progresses = Arrays.asList(
            createProgressWithKnowledge(KnowledgeLevel.BIEN),
            createProgressWithKnowledge(KnowledgeLevel.REGULAR),
            createProgressWithKnowledge(null) // Sin hacer
        );
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userFlashcardProgressRepository.findByCollectionIdAndUserId(anyLong(), anyString())).thenReturn(progresses);

        // When
        FlashcardStatsDto result = flashcardService.getFlashcardStats(1L, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getTotalFlashcards());
        
        // Verificar estadísticas por nivel de conocimiento
        assertNotNull(result.getEstadosPorConocimiento());
        assertEquals(3, result.getEstadosPorConocimiento().size());
        
        // Verificar que los mapas de conteo están inicializados
        assertNotNull(result.getKnowledgeLevelCounts());
        assertNotNull(result.getStatusCounts());
        
        verify(userRepository).findByEmail("test@example.com");
        verify(userFlashcardProgressRepository).findByCollectionIdAndUserId(1L, "test-user-id");
    }

    @Test
    void getFlashcardStats_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> flashcardService.getFlashcardStats(1L, "nonexistent@example.com")
        );
        
        assertEquals("Usuario no encontrado con email: nonexistent@example.com", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void createFlashcard_ShouldThrowException_WhenCollectionNotFound() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> flashcardService.createFlashcard(999L, testFlashcardDto, "test@example.com")
        );
        
        assertEquals("Collection not found with id: 999", exception.getMessage());
        verify(collectionRepository).findById(999L);
        verifyNoInteractions(flashcardRepository);
    }

    @Test
    void updateFlashcard_ShouldReturnUpdatedFlashcard_WhenFlashcardExists() {
        // Given
        FlashcardDto expectedDto = new FlashcardDto();
        expectedDto.setQuestion(testFlashcardDto.getQuestion());
        expectedDto.setAnswer(testFlashcardDto.getAnswer());
        
        Flashcard mockFlashcard = mock(Flashcard.class);
        when(mockFlashcard.toDto()).thenReturn(expectedDto);
        
        when(flashcardRepository.findById(anyLong())).thenReturn(Optional.of(testFlashcard));
        when(flashcardRepository.save(any(Flashcard.class))).thenReturn(mockFlashcard);

        // When
        FlashcardDto result = flashcardService.updateFlashcard(1L, testFlashcardDto);

        // Then
        assertNotNull(result);
        assertEquals(testFlashcardDto.getQuestion(), result.getQuestion());
        assertEquals(testFlashcardDto.getAnswer(), result.getAnswer());
        verify(flashcardRepository).findById(1L);
        verify(flashcardRepository).save(testFlashcard);
    }

    @Test
    void updateFlashcard_ShouldThrowException_WhenFlashcardNotFound() {
        // Given
        when(flashcardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> flashcardService.updateFlashcard(999L, testFlashcardDto)
        );
        
        assertEquals("Flashcard not found", exception.getMessage());
        verify(flashcardRepository).findById(999L);
        verify(flashcardRepository, never()).save(any());
    }

    @Test
    void deleteFlashcard_ShouldDeleteFlashcard_WhenFlashcardExists() {
        // Given
        when(flashcardRepository.findById(anyLong())).thenReturn(Optional.of(testFlashcard));

        // When
        flashcardService.deleteFlashcard(1L);

        // Then
        verify(flashcardRepository).findById(1L);
        verify(flashcardRepository).delete(testFlashcard);
    }

    @Test
    void deleteFlashcard_ShouldThrowException_WhenFlashcardNotFound() {
        // Given
        when(flashcardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> flashcardService.deleteFlashcard(999L)
        );
        
        assertEquals("Flashcard not found", exception.getMessage());
        verify(flashcardRepository).findById(999L);
        verify(flashcardRepository, never()).delete(any());
    }

    @Test
    void processReview_ShouldProcessCorrectReview_WhenInputIsValid() {
        // Given
        FlashcardReviewDto reviewDto = new FlashcardReviewDto();
        reviewDto.setResult("BIEN");
        reviewDto.setTimeSpentMs(5000L);
        
        when(flashcardRepository.findById(anyLong())).thenReturn(Optional.of(testFlashcard));
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(FlashcardReview.class))).thenReturn(new FlashcardReview());
        when(userFlashcardProgressRepository.findByFlashcardIdAndUserId(anyLong(), anyString())).thenReturn(Optional.of(testProgress));
        when(userFlashcardProgressRepository.save(any(UserFlashcardProgress.class))).thenReturn(testProgress);

        // When
        Flashcard result = flashcardService.processReview(1L, reviewDto, "test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(testFlashcard.getId(), result.getId());
        
        verify(flashcardRepository).findById(1L);
        verify(userRepository).findById("test-user-id");
        verify(reviewRepository).save(any(FlashcardReview.class));
        verify(userFlashcardProgressRepository).save(any(UserFlashcardProgress.class));
    }

    @Test
    void processReview_ShouldThrowException_WhenFlashcardNotFound() {
        // Given
        FlashcardReviewDto reviewDto = new FlashcardReviewDto();
        reviewDto.setResult("BIEN");
        reviewDto.setTimeSpentMs(5000L);
        
        when(flashcardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> flashcardService.processReview(999L, reviewDto, "test-user-id")
        );
        
        assertEquals("Flashcard not found", exception.getMessage());
        verify(flashcardRepository).findById(999L);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void updateUserFlashcardProgress_ShouldUpdateProgressCorrectly_WhenCorrectAnswer() {
        // Given
        when(userFlashcardProgressRepository.findByFlashcardIdAndUserId(anyLong(), anyString())).thenReturn(Optional.of(testProgress));
        when(userFlashcardProgressRepository.save(any(UserFlashcardProgress.class))).thenReturn(testProgress);

        int originalSuccessCount = testProgress.getSuccessCount();
        int originalReviewCount = testProgress.getReviewCount();

        // When
        flashcardService.updateUserFlashcardProgress(testUser, testFlashcard, "BIEN", 5000L);

        // Then
        verify(userFlashcardProgressRepository).findByFlashcardIdAndUserId(1L, "test-user-id");
        verify(userFlashcardProgressRepository).save(testProgress);
        
        // Verificar que el progreso se actualizó correctamente
        assertEquals(KnowledgeLevel.BIEN, testProgress.getKnowledgeLevel());
        assertEquals(originalSuccessCount + 1, testProgress.getSuccessCount());
        assertEquals(originalReviewCount + 1, testProgress.getReviewCount());
    }

    @Test
    void updateUserFlashcardProgress_ShouldCreateNewProgress_WhenNoProgressExists() {
        // Given
        when(userFlashcardProgressRepository.findByFlashcardIdAndUserId(anyLong(), anyString())).thenReturn(Optional.empty());
        when(userFlashcardProgressRepository.save(any(UserFlashcardProgress.class))).thenReturn(testProgress);

        // When
        flashcardService.updateUserFlashcardProgress(testUser, testFlashcard, "BIEN", 5000L);

        // Then
        verify(userFlashcardProgressRepository).findByFlashcardIdAndUserId(1L, "test-user-id");
        verify(userFlashcardProgressRepository).save(any(UserFlashcardProgress.class));
    }

    @Test
    void updateUserFlashcardProgress_ShouldHandleWrongAnswer_WhenAnswerIsIncorrect() {
        // Given
        when(userFlashcardProgressRepository.findByFlashcardIdAndUserId(anyLong(), anyString())).thenReturn(Optional.of(testProgress));
        when(userFlashcardProgressRepository.save(any(UserFlashcardProgress.class))).thenReturn(testProgress);

        int originalFailureCount = testProgress.getFailureCount();

        // When
        flashcardService.updateUserFlashcardProgress(testUser, testFlashcard, "MAL", 5000L);

        // Then
        verify(userFlashcardProgressRepository).save(testProgress);
        
        // Verificar que el progreso se actualizó correctamente para respuesta incorrecta
        assertEquals(KnowledgeLevel.MAL, testProgress.getKnowledgeLevel());
        assertEquals(originalFailureCount + 1, testProgress.getFailureCount());
        assertEquals(0, testProgress.getRepetitionLevel()); // Debería reiniciarse
    }

    // Método auxiliar para crear progreso con nivel de conocimiento específico
    private UserFlashcardProgress createProgressWithKnowledge(KnowledgeLevel level) {
        UserFlashcardProgress progress = new UserFlashcardProgress();
        progress.setKnowledgeLevel(level);
        progress.setUser(testUser);
        progress.setFlashcard(testFlashcard);
        progress.setCollection(testCollection);
        return progress;
    }
} 