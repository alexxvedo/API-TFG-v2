package com.example.api_v2.service;

import com.example.api_v2.dto.FlashcardDto;
import com.example.api_v2.dto.FlashcardGenerationDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {

    @Value("${google.ai.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final GeminiClient geminiClient;

    public AIService(ObjectMapper objectMapper, GeminiClient geminiClient) {
        this.objectMapper = objectMapper;
        this.geminiClient = geminiClient;
    }

    public Map<String, Object> generateFlashcards(FlashcardGenerationDto request) {
        try {
            String prompt = buildPrompt(request);
            String response = geminiClient.getResponseFromGemini(prompt)
                    .block(); // Convertimos el Mono a blocking para mantener la compatibilidad

            List<FlashcardDto> flashcards = parseResponse(response);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("validCount", flashcards.size());
            metadata.put("totalGenerated", flashcards.size());

            Map<String, Object> result = new HashMap<>();
            result.put("flashcards", flashcards);
            result.put("metadata", metadata);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error generating flashcards: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(FlashcardGenerationDto request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            You are an AI assistant that creates educational flashcards.
            Generate flashcards about the following topic:
            """)
                .append(request.getUserPrompt())
                .append("\n\n");

        if (request.getContextPrompt() != null && !request.getContextPrompt().isEmpty()) {
            prompt.append("Additional context: ").append(request.getContextPrompt()).append("\n\n");
        }

        if (request.getGeneratedFlashcardsHistory() != null && !request.getGeneratedFlashcardsHistory().isEmpty()) {
            prompt.append("Previously generated flashcards (avoid duplicating this content):\n");
            request.getGeneratedFlashcardsHistory().forEach(card -> {
                prompt.append("Q: ").append(card.getQuestion()).append("\n");
                prompt.append("A: ").append(card.getAnswer()).append("\n\n");
            });
        }

        prompt.append("""
            Instructions:
            1. Generate exactly 5 unique, high-quality flashcards
            2. Each flashcard should have a clear, concise question
            3. Answers should be detailed but not too long (max 2-3 sentences)
            4. Format your response EXACTLY as a JSON array of objects with this structure:
               [
                 {
                   "question": "What is...?",
                   "answer": "The answer is...",
                   "difficulty": 3,
                   "topic": "subtopic"
                 }
               ]
            5. Ensure content is accurate and educational
            6. Do not duplicate content from previously generated flashcards
            7. Make questions engaging and thought-provoking
            8. IMPORTANT: Response must be valid JSON that can be parsed
            9. DO NOT include markdown code block markers (```json)
            """);

        return prompt.toString();
    }

    private List<FlashcardDto> parseResponse(String response) {
        try {
            // Extraer el JSON de la respuesta markdown si existe
            String jsonContent = extractJsonFromMarkdown(response);

            // Parseamos el contenido JSON a una lista de FlashcardDto
            return objectMapper.readValue(jsonContent, new TypeReference<List<FlashcardDto>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing AI response: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromMarkdown(String response) {
        // Primero intentamos encontrar el bloque JSON dentro de ```json ... ```
        Pattern pattern = Pattern.compile("```json\\s*\\n(.*?)\\n\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Si no encontramos el bloque markdown, asumimos que es JSON directo
        return response.trim();
    }
}
