package com.example.api_v2.service;

import com.example.api_v2.dto.DocumentDto;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Document;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.DocumentRepository;
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
 * Tests unitarios para DocumentService
 * Valida la lógica de negocio relacionada con documentos
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private DocumentService documentService;

    private Collection testCollection;
    private Document testDocument;
    private DocumentDto testDocumentDto;

    @BeforeEach
    void setUp() {
        testCollection = new Collection();
        testCollection.setId(1L);
        testCollection.setName("Test Collection");

        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setFileName("test-document.pdf");
        testDocument.setFileType("pdf");
        testDocument.setFileSize(1024L);
        testDocument.setContent("Test document content");
        testDocument.setCollection(testCollection);

        testDocumentDto = new DocumentDto(testDocument);

    }

    @Test
    void getDocumentsByCollection_ShouldReturnDocumentList_WhenDocumentsExist() {
        // Given
        List<Document> documents = List.of(testDocument);
        when(documentRepository.findByCollectionId(anyLong())).thenReturn(documents);

        // When
        List<DocumentDto> result = documentService.getDocumentsByCollection(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        DocumentDto documentDto = result.get(0);
        assertEquals(testDocument.getFileName(), documentDto.getFileName());
        assertEquals(testDocument.getFileType(), documentDto.getFileType());
        assertEquals(testDocument.getFileSize(), documentDto.getFileSize());
        
        verify(documentRepository).findByCollectionId(1L);
    }

    @Test
    void getDocumentsByCollection_ShouldReturnEmptyList_WhenNoDocumentsExist() {
        // Given
        when(documentRepository.findByCollectionId(anyLong())).thenReturn(new ArrayList<>());

        // When
        List<DocumentDto> result = documentService.getDocumentsByCollection(1L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(documentRepository).findByCollectionId(1L);
    }

    @Test
    void getDocument_ShouldReturnDocument_WhenDocumentExists() {
        // Given
        when(documentRepository.findById(anyLong())).thenReturn(Optional.of(testDocument));

        // When
        Optional<Document> result = documentService.getDocument(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(testDocument.getId(), result.get().getId());
        assertEquals(testDocument.getFileName(), result.get().getFileName());
        assertEquals(testDocument.getFileType(), result.get().getFileType());
        
        verify(documentRepository).findById(1L);
    }

    @Test
    void getDocument_ShouldReturnEmpty_WhenDocumentNotFound() {
        // Given
        when(documentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        Optional<Document> result = documentService.getDocument(999L);

        // Then
        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(documentRepository).findById(999L);
    }

    @Test
    void deleteDocument_ShouldDeleteDocument_WhenCalled() {
        // When
        documentService.deleteDocument(1L);

        // Then
        verify(documentRepository).deleteById(1L);
    }
} 