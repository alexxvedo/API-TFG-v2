package com.example.api_v2.service;

import com.example.api_v2.dto.DocumentDto;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Document;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CollectionRepository collectionRepository;
    private final AgentService agentService;  // 🔹 Ahora usamos el servicio del agente


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



        // 🔹 Enviar documento al agente para indexación y análisis
        agentService.processDocument(savedDocument.getId().toString(), collectionId.toString(), file.getBytes())
                .doOnError(error -> {
                    System.err.println("Error al procesar el documento en el agente: " + error.getMessage());
                    // Aquí podrías agregar más lógica de manejo de errores si es necesario
                })
                .subscribe();

        return savedDocument;
    }


    

    @Transactional(readOnly = true)
    public List<DocumentDto> getDocumentsByCollection(Long collectionId) {
        return documentRepository.findByCollectionId(collectionId)
                .stream()
                .map(DocumentDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Document> getDocument(Long documentId) {
        return documentRepository.findById(documentId);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        documentRepository.deleteById(documentId);
    }
}
