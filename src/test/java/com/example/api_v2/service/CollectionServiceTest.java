package com.example.api_v2.service;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.User;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.WorkspaceActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CollectionService
 * Valida la lógica de negocio relacionada con colecciones
 */
@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceActivityRepository workspaceActivityRepository;

    @Mock
    private FlashcardService flashcardService;

    @InjectMocks
    private CollectionService collectionService;

    private Collection testCollection;
    private Workspace testWorkspace;
    private User testUser;
    private WorkspaceUser testWorkspaceUser;
    private CollectionDto testCollectionDto;

    @BeforeEach
    void setUp() {
        // Setup User
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Setup Workspace
        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");
        testWorkspace.setDescription("Test Description");

        // Setup WorkspaceUser
        testWorkspaceUser = new WorkspaceUser();
        testWorkspaceUser.setUser(testUser);

        // Setup Collection
        testCollection = new Collection();
        testCollection.setId(1L);
        testCollection.setName("Test Collection");
        testCollection.setDescription("Test Collection Description");
        testCollection.setWorkspace(testWorkspace);
        testCollection.setCreatedBy(testUser);
        testCollection.setCreatedAt(LocalDateTime.now());
        testCollection.setUpdatedAt(LocalDateTime.now());

        // Setup CollectionDto
        testCollectionDto = new CollectionDto();
        testCollectionDto.setId(1L);
        testCollectionDto.setName("Test Collection");
        testCollectionDto.setDescription("Test Collection Description");
        testCollectionDto.setWorkspaceId(1L);
    }

    @Test
    void getCollectionsByWorkspace_ShouldReturnCollections_WhenWorkspaceExists() {
        // Given
        List<Collection> collections = Arrays.asList(testCollection);
        when(collectionRepository.findByWorkspaceId(anyLong())).thenReturn(collections);

        // When
        List<CollectionDto> result = collectionService.getCollectionsByWorkspace(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCollection.getName(), result.get(0).getName());
        verify(collectionRepository).findByWorkspaceId(1L);
    }

    @Test
    void getCollectionsByWorkspace_ShouldReturnEmptyList_WhenNoCollections() {
        // Given
        when(collectionRepository.findByWorkspaceId(anyLong())).thenReturn(Arrays.asList());

        // When
        List<CollectionDto> result = collectionService.getCollectionsByWorkspace(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(collectionRepository).findByWorkspaceId(1L);
    }

    @Test
    void getCollection_ShouldReturnCollection_WhenValidRequest() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // When
        CollectionDto result = collectionService.getCollection(1L, 1L, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testCollection.getName(), result.getName());
        assertEquals(testCollection.getDescription(), result.getDescription());
        verify(collectionRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getCollection_ShouldThrowException_WhenCollectionNotFound() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            collectionService.getCollection(1L, 1L, "test@example.com")
        );
        verify(collectionRepository).findById(1L);
    }

    @Test
    void getCollection_ShouldThrowException_WhenCollectionNotInWorkspace() {
        // Given
        Workspace otherWorkspace = new Workspace();
        otherWorkspace.setId(2L);
        testCollection.setWorkspace(otherWorkspace);
        
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            collectionService.getCollection(1L, 1L, "test@example.com")
        );
        verify(collectionRepository).findById(1L);
    }

    @Test
    void createCollection_ShouldCreateCollection_WhenValidRequest() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(collectionRepository.findByWorkspaceIdAndName(anyLong(), anyString())).thenReturn(Optional.empty());
        when(collectionRepository.save(any(Collection.class))).thenReturn(testCollection);

        // When
        CollectionDto result = collectionService.createCollection(1L, testCollectionDto, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testCollectionDto.getName(), result.getName());
        verify(workspaceRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(collectionRepository).findByWorkspaceIdAndName(1L, testCollectionDto.getName());
        verify(collectionRepository).save(any(Collection.class));
        verify(workspaceActivityRepository).save(any());
    }

    @Test
    void createCollection_ShouldThrowException_WhenWorkspaceNotFound() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            collectionService.createCollection(1L, testCollectionDto, "test@example.com")
        );
        verify(workspaceRepository).findById(1L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(collectionRepository);
    }

    @Test
    void createCollection_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            collectionService.createCollection(1L, testCollectionDto, "test@example.com")
        );
        verify(workspaceRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verifyNoInteractions(collectionRepository);
    }

    @Test
    void createCollection_ShouldThrowException_WhenNameIsEmpty() {
        // Given
        testCollectionDto.setName("");

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            collectionService.createCollection(1L, testCollectionDto, "test@example.com")
        );
        verifyNoInteractions(workspaceRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(collectionRepository);
    }

    @Test
    void updateCollection_ShouldUpdateCollection_WhenValidRequest() {
        // Given
        String updatedName = "Updated Collection Name";
        String updatedDescription = "Updated Description";
        
        CollectionDto updateDto = new CollectionDto();
        updateDto.setName(updatedName);
        updateDto.setDescription(updatedDescription);

        Collection updatedCollection = new Collection();
        updatedCollection.setId(1L);
        updatedCollection.setName(updatedName);
        updatedCollection.setDescription(updatedDescription);
        updatedCollection.setWorkspace(testWorkspace);
        updatedCollection.setCreatedBy(testUser);

        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));
        when(collectionRepository.findByWorkspaceIdAndName(anyLong(), anyString())).thenReturn(Optional.empty());
        when(collectionRepository.save(any(Collection.class))).thenReturn(updatedCollection);

        // When
        CollectionDto result = collectionService.updateCollection(1L, 1L, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(updatedName, result.getName());
        assertEquals(updatedDescription, result.getDescription());
        verify(collectionRepository).findById(1L);
        verify(collectionRepository).findByWorkspaceIdAndName(1L, updatedName);
        verify(collectionRepository).save(any(Collection.class));
    }

    @Test
    void deleteCollection_ShouldDeleteCollection_WhenValidRequest() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.of(testCollection));

        // When
        collectionService.deleteCollection(1L, 1L);

        // Then
        verify(collectionRepository).findById(1L);
        verify(collectionRepository).delete(testCollection);
    }

    @Test
    void deleteCollection_ShouldThrowException_WhenCollectionNotFound() {
        // Given
        when(collectionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            collectionService.deleteCollection(1L, 1L)
        );
        verify(collectionRepository).findById(1L);
        verify(collectionRepository, never()).delete(any(Collection.class));
    }
} 