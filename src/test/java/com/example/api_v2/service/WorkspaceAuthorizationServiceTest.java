package com.example.api_v2.service;

import com.example.api_v2.model.PermissionType;
import com.example.api_v2.model.User;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.WorkspaceUserRepository;
import com.example.api_v2.repository.UserRepository;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para WorkspaceAuthorizationService
 * Valida la lógica de autorización de workspaces
 */
@ExtendWith(MockitoExtension.class)
class WorkspaceAuthorizationServiceTest {

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceAuthorizationService workspaceAuthorizationService;

    private User testUser;
    private Workspace testWorkspace;
    private WorkspaceUser testWorkspaceUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");

        testWorkspaceUser = new WorkspaceUser();
        testWorkspaceUser.setUser(testUser);
        testWorkspaceUser.setWorkspace(testWorkspace);
        testWorkspaceUser.setPermissionType(PermissionType.OWNER);
    }

    @Test
    void hasWorkspaceAccess_ShouldReturnTrue_WhenUserHasAccess() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(testWorkspaceUser);

        // When
        boolean result = workspaceAuthorizationService.hasWorkspaceAccess("test@example.com", 1L);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
    }

    @Test
    void hasWorkspaceAccess_ShouldReturnFalse_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        boolean result = workspaceAuthorizationService.hasWorkspaceAccess("nonexistent@example.com", 1L);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail("nonexistent@example.com");
        verifyNoInteractions(workspaceUserRepository);
    }

    @Test
    void hasWorkspaceAccess_ShouldReturnFalse_WhenUserNotInWorkspace() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(null);

        // When
        boolean result = workspaceAuthorizationService.hasWorkspaceAccess("test@example.com", 1L);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
    }

    @Test
    void hasWorkspacePermission_ShouldReturnTrue_WhenUserHasRequiredPermission() {
        // Given
        testWorkspaceUser.setPermissionType(PermissionType.EDITOR);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(testWorkspaceUser);

        // When
        boolean result = workspaceAuthorizationService.hasWorkspacePermission("test@example.com", 1L, PermissionType.VIEWER);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
    }

    @Test
    void hasWorkspacePermission_ShouldReturnFalse_WhenUserHasInsufficientPermission() {
        // Given
        testWorkspaceUser.setPermissionType(PermissionType.VIEWER);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(testWorkspaceUser);

        // When
        boolean result = workspaceAuthorizationService.hasWorkspacePermission("test@example.com", 1L, PermissionType.OWNER);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
    }

    @Test
    void isWorkspaceOwner_ShouldReturnTrue_WhenUserIsOwner() {
        // Given
        testWorkspaceUser.setPermissionType(PermissionType.OWNER);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(testWorkspaceUser);

        // When
        boolean result = workspaceAuthorizationService.isWorkspaceOwner("test@example.com", 1L);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
    }

    @Test
    void canEditWorkspace_ShouldReturnTrue_WhenUserIsEditor() {
        // Given
        testWorkspaceUser.setPermissionType(PermissionType.EDITOR);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(testWorkspaceUser);

        // When
        boolean result = workspaceAuthorizationService.canEditWorkspace("test@example.com", 1L);

        // Then
        assertTrue(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
    }

    @Test
    void canEditWorkspace_ShouldReturnFalse_WhenUserIsOnlyViewer() {
        // Given
        testWorkspaceUser.setPermissionType(PermissionType.VIEWER);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(workspaceUserRepository.findByUserIdAndWorkspaceId(anyString(), anyLong())).thenReturn(testWorkspaceUser);

        // When
        boolean result = workspaceAuthorizationService.canEditWorkspace("test@example.com", 1L);

        // Then
        assertFalse(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(workspaceUserRepository).findByUserIdAndWorkspaceId("test-user-id", 1L);
    }
} 