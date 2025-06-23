package com.example.api_v2.service;

import com.example.api_v2.dto.UserDto;
import com.example.api_v2.dto.WorkspaceDto;
import com.example.api_v2.model.*;
import com.example.api_v2.repository.*;
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
 * Tests unitarios para WorkspaceService
 * Valida la lógica de negocio relacionada con workspaces
 */
@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFlashcardProgressRepository userFlashcardProgressRepository;

    @Mock
    private WorkspaceActivityRepository workspaceActivityRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private User testUser;
    private Workspace testWorkspace;
    private WorkspaceUser testWorkspaceUser;
    private WorkspaceDto testWorkspaceDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");
        testWorkspace.setDescription("Test Description");
        testWorkspace.setWorkspaceUsers(new ArrayList<>());
        testWorkspace.setCollections(new ArrayList<>());

        testWorkspaceUser = new WorkspaceUser();
        testWorkspaceUser.setId(1L);
        testWorkspaceUser.setUser(testUser);
        testWorkspaceUser.setWorkspace(testWorkspace);
        testWorkspaceUser.setPermissionType(PermissionType.OWNER);

        testWorkspaceDto = new WorkspaceDto();
        testWorkspaceDto.setName("New Workspace");
        testWorkspaceDto.setDescription("New Description");
    }

    @Test
    void getWorkspacesByUserEmail_ShouldReturnWorkspaces_WhenUserExists() {
        // Given
        List<WorkspaceUser> workspaceUsers = List.of(testWorkspaceUser);
        List<Workspace> workspaces = List.of(testWorkspace);
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findAllByUserId(anyString())).thenReturn(workspaceUsers);
        when(workspaceRepository.findWorkspacesByUserId(anyString())).thenReturn(workspaces);

        // When
        List<WorkspaceDto> result = workspaceService.getWorkspacesByUserEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspace.getName(), result.get(0).getName());
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findAllByUserId("test-user-id");
        verify(workspaceRepository).findWorkspacesByUserId("test-user-id");
    }

    @Test
    void getWorkspacesByUserEmail_ShouldCreateDefaultWorkspace_WhenUserHasNoWorkspaces() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findAllByUserId(anyString())).thenReturn(new ArrayList<>());
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(testWorkspace);
        when(workspaceUserRepository.save(any(WorkspaceUser.class))).thenReturn(testWorkspaceUser);
        when(workspaceActivityRepository.save(any(WorkspaceActivity.class))).thenReturn(new WorkspaceActivity());
        when(workspaceRepository.findWorkspacesByUserId(anyString())).thenReturn(List.of(testWorkspace));

        // When
        List<WorkspaceDto> result = workspaceService.getWorkspacesByUserEmail("test@example.com");

        // Then
        assertNotNull(result);
        verify(workspaceRepository, atLeastOnce()).save(any(Workspace.class));
        verify(workspaceUserRepository).save(any(WorkspaceUser.class));
    }

    @Test
    void getWorkspacesByUserEmail_ShouldThrowException_WhenEmailIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workspaceService.getWorkspacesByUserEmail(null)
        );
        
        assertEquals("email no puede ser nulo o vacío", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void getWorkspacesByUserEmail_ShouldThrowException_WhenEmailIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workspaceService.getWorkspacesByUserEmail("")
        );
        
        assertEquals("email no puede ser nulo o vacío", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void getWorkspacesByUserEmail_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> workspaceService.getWorkspacesByUserEmail("nonexistent@example.com")
        );
        
        assertEquals("Usuario no encontrado con email: nonexistent@example.com", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void getWorkspace_ShouldReturnWorkspaceDto_WhenWorkspaceExists() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));

        // When
        WorkspaceDto result = workspaceService.getWorkspace(1L);

        // Then
        assertNotNull(result);
        assertEquals(testWorkspace.getId(), result.getId());
        assertEquals(testWorkspace.getName(), result.getName());
        verify(workspaceRepository).findById(1L);
    }

    @Test
    void getWorkspace_ShouldThrowException_WhenWorkspaceNotFound() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> workspaceService.getWorkspace(1L)
        );
        
        assertEquals("Workspace not found", exception.getMessage());
        verify(workspaceRepository).findById(1L);
    }

    @Test
    void createWorkspace_ShouldReturnWorkspaceDto_WhenValidInput() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(testWorkspace);
        when(workspaceUserRepository.save(any(WorkspaceUser.class))).thenReturn(testWorkspaceUser);
        when(workspaceActivityRepository.save(any(WorkspaceActivity.class))).thenReturn(new WorkspaceActivity());

        // When
        WorkspaceDto result = workspaceService.createWorkspace(testWorkspaceDto, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testWorkspace.getId(), result.getId());
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceRepository, times(2)).save(any(Workspace.class));
        verify(workspaceUserRepository).save(any(WorkspaceUser.class));
        verify(workspaceActivityRepository).save(any(WorkspaceActivity.class));
    }

    @Test
    void updateWorkspace_ShouldReturnUpdatedWorkspaceDto_WhenWorkspaceExists() {
        // Given
        WorkspaceDto updateDto = new WorkspaceDto();
        updateDto.setName("Updated Name");
        
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(testWorkspace);

        // When
        WorkspaceDto result = workspaceService.updateWorkspace(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(workspaceRepository).findById(1L);
        verify(workspaceRepository).save(testWorkspace);
    }

    @Test
    void updateWorkspace_ShouldThrowException_WhenWorkspaceNotFound() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> workspaceService.updateWorkspace(1L, testWorkspaceDto)
        );
        
        assertEquals("Workspace not found", exception.getMessage());
        verify(workspaceRepository).findById(1L);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void deleteWorkspace_ShouldCallRepository_WhenCalled() {
        // When
        workspaceService.deleteWorkspace(1L);

        // Then
        verify(workspaceRepository).deleteById(1L);
    }

    @Test
    void joinWorkspace_ShouldAddUserToWorkspace_WhenValidInput() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(null);
        when(workspaceUserRepository.save(any(WorkspaceUser.class))).thenReturn(testWorkspaceUser);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(testWorkspace);

        // When
        workspaceService.joinWorkspace(1L, "test@example.com", PermissionType.VIEWER);

        // Then
        verify(workspaceRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
        verify(workspaceUserRepository).save(any(WorkspaceUser.class));
        verify(workspaceRepository).save(testWorkspace);
    }

    @Test
    void joinWorkspace_ShouldThrowException_WhenUserAlreadyJoined() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(testWorkspaceUser);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> workspaceService.joinWorkspace(1L, "test@example.com", PermissionType.VIEWER)
        );
        
        assertEquals("User already joined the workspace", exception.getMessage());
        verify(workspaceUserRepository, never()).save(any());
    }

    @Test
    void getWorkspaceUsers_ShouldReturnUserList_WhenWorkspaceExists() {
        // Given
        testWorkspace.getWorkspaceUsers().add(testWorkspaceUser);
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));

        // When
        List<UserDto> result = workspaceService.getWorkspaceUsers(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        assertEquals(testUser.getEmail(), result.get(0).getEmail());
        assertEquals(PermissionType.OWNER, result.get(0).getPermissionType());
        verify(workspaceRepository).findById(1L);
    }

    @Test
    void getWorkspaceUsers_ShouldThrowException_WhenWorkspaceNotFound() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> workspaceService.getWorkspaceUsers(1L)
        );
        
        assertEquals("Workspace not found", exception.getMessage());
        verify(workspaceRepository).findById(1L);
    }
} 