package com.example.api_v2.service;

import com.example.api_v2.dto.DocumentDto;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Document;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CollectionRepository collectionRepository;
    private final AgentService agentService; // 🔹 Ahora usamos el servicio del agente
    private final WorkspaceActivityService workspaceActivityService;

    @Transactional
    public Document uploadFile(Long collectionId, MultipartFile file) throws IOException {
        // Obtener la colección asociada
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        // Crear y guardar el documento
        Document document = new Document();
        document.setCollection(collection);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setData(file.getBytes());

        Document savedDocument = documentRepository.save(document);

        // Registrar la actividad
        // workspaceActivityService.logDocumentUploaded(
        //     collection.getWorkspace().getId(), 
        //     userEmail, 
        //     file.getOriginalFilename(), 
        //     collection.getName()
        // );

        // 🔹 Enviar documento al agente para indexación y análisis
        agentService.processDocument(file.getBytes())
                .doOnError(error -> {
                    System.err.println("Error al procesar el documento en el agente: " + error.getMessage());
                    // Aquí podrías agregar más lógica de manejo de errores si es necesario
                }).flatMap((Map<String, Object> response) -> {
                    @SuppressWarnings("unchecked")
                    List<Number> embeddingList = (List<Number>) response.get("embedding");
                    float[] embedding = new float[embeddingList.size()];
                    for (int i = 0; i < embeddingList.size(); i++) {
                        embedding[i] = embeddingList.get(i).floatValue();
                    }
                    String content = (String) response.get("content");
                    System.out.println(content);
                    savedDocument.setContent(content);
                    savedDocument.setEmbedding(embedding);
                    return Mono.fromCallable(() -> documentRepository.save(savedDocument));
                })

                .subscribe();

        return savedDocument;
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> getDocumentsByCollection(Long collectionId) {

        List<Document> documentos = documentRepository.findByCollectionId(collectionId);

        if (documentos.isEmpty()) {
            return Collections.emptyList();
        } else {
            return documentos
                    .stream()
                    .map(DocumentDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public Optional<Document> getDocument(Long documentId) {
        return documentRepository.findById(documentId);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        // Obtener el documento antes de eliminarlo para logging
        Optional<Document> documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isPresent()) {
            Document document = documentOpt.get();
            Collection collection = document.getCollection();
            
            documentRepository.deleteById(documentId);
            
            // Registrar la actividad
        //     workspaceActivityService.logDocumentDeleted(
        //         collection.getWorkspace().getId(), 
        //         userEmail, 
        //         document.getFileName(), 
        //         collection.getName()
        //     );
        } else {
            documentRepository.deleteById(documentId);
        }
    }
}
