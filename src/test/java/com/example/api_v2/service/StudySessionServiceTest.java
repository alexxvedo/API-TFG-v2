package com.example.api_v2.service;

import com.example.api_v2.dto.StudySessionDto;
import com.example.api_v2.dto.UserDto;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.*;
import com.example.api_v2.model.Collection;
import com.example.api_v2.repository.StudySessionRepository;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para StudySessionService
 * Valida la lógica de negocio relacionada con sesiones de estudio
 */
@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private FlashcardService flashcardService;

    @InjectMocks
    private StudySessionService studySessionService;

    private User testUser;
    private Collection testCollection;
    private StudySession testStudySession;
    private StudySessionDto testStudySessionDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCollection = new Collection();
        testCollection.setId(1L);
        testCollection.setName("Test Collection");

        testStudySession = new StudySession();
        testStudySession.setId(1L);
        testStudySession.setUser(testUser);
        testStudySession.setCollection(testCollection);
        testStudySession.setStartTime(LocalDateTime.now().minusHours(1));
        testStudySession.setEndTime(LocalDateTime.now());
        testStudySession.setTotalCards(10);
        testStudySession.setCorrectAnswers(8);
        testStudySession.setIncorrectAnswers(2);

        testStudySessionDto = new StudySessionDto();
        testStudySessionDto.setCollectionId(1L);
        testStudySessionDto.setTotalCards(15);
        testStudySessionDto.setCorrectAnswers(12);
        testStudySessionDto.setIncorrectAnswers(3);
        UserDto userDto = new UserDto();
        userDto.setId(testUser.getId());
        userDto.setEmail(testUser.getEmail());
        userDto.setName(testUser.getName());
        testStudySessionDto.setUser(userDto);
    }

    @Test
    void getStudySession_ShouldReturnStudySessionDto_WhenSessionExists() {
        // Given
        when(studySessionRepository.findById(anyLong())).thenReturn(Optional.of(testStudySession));

        // When
        StudySessionDto result = studySessionService.getStudySession(1L);

        // Then
        assertNotNull(result);
        assertEquals(testStudySession.getId(), result.getId());
        assertEquals(testStudySession.getCollection().getId(), result.getCollectionId());
        
        verify(studySessionRepository).findById(1L);
    }

    @Test
    void createStudySession_ShouldReturnStudySessionDto_WhenValidInput() {
        // Given
        testStudySessionDto.getUser().setEmail("test@example.com");
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(testStudySession);

        // When
        StudySessionDto result = studySessionService.createStudySession(testStudySessionDto);

        // Then
        assertNotNull(result);
        assertEquals(testStudySession.getId(), result.getId());
        assertEquals(testStudySession.getCollection().getId(), result.getCollectionId());
        
        verify(collectionRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(studySessionRepository).save(any(StudySession.class));
    }

    @Test
    void completeStudySession_ShouldReturnCompletedSession_WhenSessionExists() {
        // Given
        UserStats userStats = new UserStats();
        userStats.setUser(testUser);
        userStats.setStudySessionsCompleted(0);
        
        when(studySessionRepository.findById(anyLong())).thenReturn(Optional.of(testStudySession));
        when(userStatsRepository.findByUserId(anyString())).thenReturn(Optional.of(userStats));
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(userStats);
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(testStudySession);

        // When
        StudySessionDto result = studySessionService.completeStudySession(1L);

        // Then
        assertNotNull(result);
        assertEquals(testStudySession.getId(), result.getId());
        verify(studySessionRepository).findById(1L);
        verify(userStatsRepository).findByUserId(testUser.getId());
        verify(studySessionRepository).save(testStudySession);
    }
} 