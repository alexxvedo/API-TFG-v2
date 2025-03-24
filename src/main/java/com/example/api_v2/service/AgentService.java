package com.example.api_v2.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // Importación agregada
import com.example.api_v2.repository.DocumentRepository;
import com.example.api_v2.model.Document;
import java.util.HashMap;
import com.example.api_v2.repository.AgentRepository;

@Service
public class AgentService {
    private final WebClient webClient;
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final AgentRepository agentRepository;

    public AgentService(DocumentRepository documentRepository, EmbeddingService embeddingService, AgentRepository agentRepository) {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")  // Conecta con agent.py
                .build(); 
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
        this.agentRepository = agentRepository;
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
        // Paso 1: Generar embedding de la pregunta usando el servicio Python
        return embeddingService.getEmbeddings(question)
                .flatMap(embedding -> {
                    // Paso 2: Buscar documentos similares en la base de datos usando Java
                    // Convertir List<Float> a float[] y luego a formato vector PostgreSQL
                    float[] embeddingArray = new float[embedding.size()];
                    for (int i = 0; i < embedding.size(); i++) {
                        embeddingArray[i] = embedding.get(i);
                    }
                    
                    // Convertir el array a formato vector PostgreSQL '[1,2,3]'
                    StringBuilder vectorStr = new StringBuilder("[");
                    for (int i = 0; i < embeddingArray.length; i++) {
                        if (i > 0) vectorStr.append(",");
                        vectorStr.append(embeddingArray[i]);
                    }
                    vectorStr.append("]");
                    
                    // Buscar documentos similares
                    List<Object[]> similarDocuments = agentRepository.findSimilarDocuments(collectionId, vectorStr.toString(), 5);
                    
                    // Preparar los documentos para enviarlos al agente Python
                    List<Map<String, Object>> formattedDocuments = similarDocuments.stream()
                            .map(doc -> {
                                String documentId = doc[0].toString();
                                String content = (String) doc[1];
                                String fileName = (String) doc[2];
                                String fileType = (String) doc[3];
                                Double similarityScore = ((Number) doc[4]).doubleValue();

                                Map<String, Object> docMap = new HashMap<>();
                                docMap.put("document_id", documentId);
                                docMap.put("content", content);
                                docMap.put("file_name", fileName);
                                docMap.put("file_type", fileType);
                                docMap.put("similarity_score", similarityScore);

                                // Extraer líneas relevantes del contenido
                                String[] lines = content.split("\n");
                                List<Map<String, Object>> relevantLines = new ArrayList<>();
                                for (int i = 0; i < lines.length; i++) {
                                    String line = lines[i];
                                    // Si la línea contiene palabras de la pregunta, la consideramos relevante
                                    if (containsRelevantTerms(line, question)) {
                                        Map<String, Object> lineInfo = new HashMap<>();
                                        lineInfo.put("line_number", i + 1);
                                        lineInfo.put("content", line);
                                        relevantLines.add(lineInfo);
                                    }
                                }
                                docMap.put("relevant_lines", relevantLines);

                                return docMap;
                            })
                            .toList();
                    
                    // Paso 3: Enviar la pregunta y los documentos similares al agente Python
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("question", question);
                    requestBody.put("similar_documents", formattedDocuments);
                    
                    System.out.println("Enviando pregunta y documentos similares al agente Python");
                    
                    return webClient.post()
                            .uri("/answer-question/")
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
                });
    }

    private boolean containsRelevantTerms(String line, String question) {
        // Convertir tanto la línea como la pregunta a minúsculas para comparación
        line = line.toLowerCase();
        String[] questionTerms = question.toLowerCase().split("\\s+");
        
        // Contar cuántos términos de la pregunta aparecen en la línea
        int matchCount = 0;
        for (String term : questionTerms) {
            if (term.length() > 3 && line.contains(term)) { // Ignorar palabras muy cortas
                matchCount++;
            }
        }
        
        // Considerar relevante si al menos 2 términos de la pregunta aparecen en la línea
        return matchCount >= 2;
    }

    public Mono<Map<String, Object>> getBriefSummary(String collectionId, String documentId) {
        Document document = documentRepository.findById(Long.parseLong(documentId))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Crear un mapa con los datos que espera el agente Python
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
        
        return webClient.post()
                .uri("/generate-brief-summary/")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> getLongSummary(String collectionId, String documentId){
        Document document = documentRepository.findById(Long.parseLong(documentId))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Crear un mapa con los datos que espera el agente Python
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
