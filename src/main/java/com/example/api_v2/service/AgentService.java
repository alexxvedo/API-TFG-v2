package com.example.api_v2.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.example.api_v2.repository.DocumentRepository;
import com.example.api_v2.model.Document;
import java.util.HashMap;

@Service
public class AgentService {
    private final WebClient webClient;
    private final DocumentRepository documentRepository;

    public AgentService(DocumentRepository documentRepository) {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")  // Conecta con agent.py
                .build(); 
        this.documentRepository = documentRepository;
    }

    // Enviar documento al agente para procesamiento
    public Mono<Map<String, Object>> processDocument(byte[] fileContent) {
        // Convertir el contenido del archivo a Base64
        String base64Content = Base64.getEncoder().encodeToString(fileContent);
        
        return webClient.post()
                .uri("/process-document/")
                .bodyValue(Map.of(
                        "pdf_base64", base64Content
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    // Generar flashcards desde un documento
    public Mono<List<Map<String, Object>>> generateFlashcardsFromDocument(String collectionId, String documentId, int numFlashcards) {
        
        Document document = documentRepository.findById(Long.parseLong(documentId))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Crear un mapa con los datos que espera el agente Python
        // El modelo Document en Python espera un campo 'content'
        HashMap<String, Object> requestBody = new HashMap<>();
        
        // Usar el campo content que ya contiene el texto extraído
        String documentContent = document.getContent();
        
        // Si el contenido es nulo o vacío, podríamos intentar extraerlo de los bytes
        if (documentContent == null || documentContent.isEmpty()) {
            // Fallback: intentar convertir los bytes a texto (esto podría no funcionar para PDFs)
            documentContent = "No se pudo extraer el contenido del documento";
            System.out.println("Advertencia: El documento no tiene contenido extraído");
        }
        
        requestBody.put("content", documentContent);

        System.out.println("Enviando contenido del documento al agente Python: " + 
                (documentContent != null ? documentContent.substring(0, Math.min(100, documentContent.length())) + "..." : "null"));

        // Enviar el contenido del documento al agente Python
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/generate-flashcards/")
                    .queryParam("num_flashcards", numFlashcards)
                    .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    // Generar flashcards desde una colección
    public Mono<List<Map<String, Object>>> generateFlashcardsFromCollection(String collectionId, int numFlashcards) {
        return webClient.get()
                .uri("/generate-flashcards/{collectionId}?num_flashcards={numFlashcards}",
                     collectionId, numFlashcards)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    // Preguntar al agente
    public Mono<Map<String, Object>> askAgent(String collectionId, String question) {
        return webClient.post()
                .uri("/ask-agent/")
                .bodyValue(Map.of(
                        "collection_id", collectionId,
                        "question", question
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }


    public Mono<Map<String, Object>> getBriefSummary(String collectionId, String documentId) {
        Document document = documentRepository.findById(Long.parseLong(documentId))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Crear un mapa con los datos que espera el agente Python
        // El modelo Document en Python espera un campo 'content'
        HashMap<String, Object> requestBody = new HashMap<>();
        
        // Usar el campo content que ya contiene el texto extraído
        String documentContent = document.getContent();
        
        // Si el contenido es nulo o vacío, podríamos intentar extraerlo de los bytes
        if (documentContent == null || documentContent.isEmpty()) {
            // Fallback: intentar convertir los bytes a texto (esto podría no funcionar para PDFs)
            documentContent = "No se pudo extraer el contenido del documento";
            System.out.println("Advertencia: El documento no tiene contenido extraído");
        }
        
        requestBody.put("content", documentContent);

        System.out.println("Enviando contenido del documento al agente Python: " + 
                (documentContent != null ? documentContent.substring(0, Math.min(100, documentContent.length())) + "..." : "null"));

        // Enviar el contenido del documento al agente Python
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/generate-brief-summary/")
                    .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> getLongSummary(String collectionId, String documentId){
        Document document = documentRepository.findById(Long.parseLong(documentId))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Crear un mapa con los datos que espera el agente Python
        // El modelo Document en Python espera un campo 'content'
        HashMap<String, Object> requestBody = new HashMap<>();
        
        // Usar el campo content que ya contiene el texto extraído
        String documentContent = document.getContent();
        
        // Si el contenido es nulo o vacío, podríamos intentar extraerlo de los bytes
        if (documentContent == null || documentContent.isEmpty()) {
            // Fallback: intentar convertir los bytes a texto (esto podría no funcionar para PDFs)
            documentContent = "No se pudo extraer el contenido del documento";
            System.out.println("Advertencia: El documento no tiene contenido extraído");
        }
        
        requestBody.put("content", documentContent);

        System.out.println("Enviando contenido del documento al agente Python: " + 
                (documentContent != null ? documentContent.substring(0, Math.min(100, documentContent.length())) + "..." : "null"));

        // Enviar el contenido del documento al agente Python
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/generate-detailed-summary/")
                    .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
