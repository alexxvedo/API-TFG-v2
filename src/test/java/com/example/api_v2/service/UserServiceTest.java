package com.example.api_v2.service;

import com.example.api_v2.dto.UserDto;
import com.example.api_v2.model.User;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserService
 * Valida la lógica de negocio relacionada con usuarios
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @Mock
    private WorkspaceService workspaceService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private WorkspaceUser testWorkspaceUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setImage("https://example.com/avatar.jpg");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testWorkspaceUser = new WorkspaceUser();
        testWorkspaceUser.setUser(testUser);
    }

    @Test
    void getUser_ShouldReturnUserDto_WhenUserExists() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUser("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findById("test-user-id");
    }

    @Test
    void getUser_ShouldReturnNull_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        UserDto result = userService.getUser("nonexistent-id");

        // Then
        assertNull(result);
        verify(userRepository).findById("nonexistent-id");
    }

    @Test
    void createUser_ShouldReturnNull_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        User result = userService.createUser("nonexistent-id");

        // Then
        assertNull(result);
        verify(userRepository).findById("nonexistent-id");
        verifyNoInteractions(workspaceUserRepository);
        verifyNoInteractions(workspaceService);
    }

    @Test
    void createUser_ShouldReturnUser_WhenUserExistsWithWorkspace() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserId(anyString())).thenReturn(testWorkspaceUser);

        // When
        User result = userService.createUser("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findById("test-user-id");
        verify(workspaceUserRepository).findByUserId("test-user-id");
        verifyNoInteractions(workspaceService);
    }

    @Test
    void createUser_ShouldCreateWorkspace_WhenUserExistsWithoutWorkspace() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserId(anyString())).thenReturn(null);

        // When
        User result = userService.createUser("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).findById("test-user-id");
        verify(workspaceUserRepository).findByUserId("test-user-id");
        verify(workspaceService).createWorkspace(any(), eq(testUser.getEmail()));
    }

    @Test
    void createUser_ShouldThrowException_WhenIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> userService.createUser(null)
        );
        
        assertEquals("id no puede ser nulo o vacío", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void createUser_ShouldThrowException_WhenIdIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> userService.createUser("")
        );
        
        assertEquals("id no puede ser nulo o vacío", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class, 
            () -> userService.getUserByEmail("nonexistent@example.com")
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void deleteUser_ShouldCallRepository_WhenCalled() {
        // When
        userService.deleteUser("test-user-id");

        // Then
        verify(userRepository).deleteById("test-user-id");
    }
} 