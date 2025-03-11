package com.example.api_v2.controller;

import com.example.api_v2.dto.DocumentDto;
import com.example.api_v2.model.Document;
import com.example.api_v2.service.DocumentService;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/collections/{collectionId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(
            @PathVariable("collectionId") Long collectionId,
            @RequestParam("file") MultipartFile file) {
        try {
            documentService.uploadFile(collectionId, file);
            return ResponseEntity.ok("Archivo subido correctamente");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo");
        }
    }

    @GetMapping("")
    public ResponseEntity<List<DocumentDto>> getDocuments(@PathVariable("collectionId") Long collectionId) {
        return ResponseEntity.ok(documentService.getDocumentsByCollection(collectionId));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable("documentId") Long documentId) {
        Document document = documentService.getDocument(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String fileName = document.getFileName();
        String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

        System.out.println(" Enviando archivo: " + fileName);
        System.out.println(" Encabezado Content-Disposition: " +
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(document.getData());
    }


    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(@PathVariable("documentId") Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok("Archivo eliminado correctamente");
    }
}
