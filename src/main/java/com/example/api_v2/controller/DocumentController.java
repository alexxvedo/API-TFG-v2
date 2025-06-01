package com.example.api_v2.controller;

import com.example.api_v2.dto.DocumentDto;
import com.example.api_v2.model.Document;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/collections/{collectionId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @WorkspaceEditAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<String> uploadDocument(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Subiendo documento a la colección {}: {}", collectionId, file.getOriginalFilename());
            documentService.uploadFile(collectionId, file);
            return ResponseEntity.ok("Archivo subido correctamente");
        } catch (IOException e) {
            log.error("Error al subir archivo a la colección {}: {}", collectionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo");
        }
    }

    @GetMapping("")
    @WorkspaceAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<List<DocumentDto>> getDocuments(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId) {
        log.info("Obteniendo documentos de la colección: {}", collectionId);
        return ResponseEntity.ok(documentService.getDocumentsByCollection(collectionId));
    }

    @GetMapping("/{documentId}")
    @WorkspaceAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("documentId") Long documentId) {
        log.info("Descargando documento: {}", documentId);
        Document document = documentService.getDocument(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String fileName = document.getFileName();
        String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

        log.debug("Enviando archivo: {} con tipo: {}", fileName, document.getFileType());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(document.getData());
    }

    @DeleteMapping("/{documentId}")
    @WorkspaceEditAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<String> deleteDocument(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("documentId") Long documentId) {
        log.info("Eliminando documento: {}", documentId);
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok("Archivo eliminado correctamente");
    }
}
