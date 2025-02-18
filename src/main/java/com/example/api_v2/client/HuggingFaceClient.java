package com.example.api_v2.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HuggingFaceClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String modelName;

    public HuggingFaceClient(WebClient.Builder webClientBuilder) {
        this.apiKey = "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";  // 🔹 Reemplázalo con tu clave
        this.modelName = "sentence-transformers/all-mpnet-base-v2";  // 🔹 Modelo correcto
        this.webClient = webClientBuilder
                .baseUrl("https://api-inference.huggingface.co/models/" + modelName)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public Mono<List<Float>> getTextEmbeddings(String text) {
        Map<String, Object> requestBody = Map.of(
                "inputs", Map.of(
                        "source_sentence", text,
                        "sentences", List.of(text) // 🔹 Se repite el mismo texto para comparación
                )
        );

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<List<Double>>>() {})
                .map(embeddings -> embeddings.get(0).stream().map(Double::floatValue).toList()) // 🔹 Convertir Double a Float
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)) // 🔹 Reintenta hasta 3 veces con 2s de espera
                        .filter(ex -> ex instanceof WebClientResponseException.ServiceUnavailable))
                .doOnError(error -> System.err.println("❌ Error en Hugging Face: " + error.getMessage()));
    }

    private List<String> splitTextIntoChunks(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+"); // Divide por espacios

        StringBuilder chunk = new StringBuilder();
        for (String word : words) {
            if (chunk.length() + word.length() > maxLength) {
                chunks.add(chunk.toString());
                chunk.setLength(0); // Reiniciar el chunk
            }
            chunk.append(word).append(" ");
        }
        if (chunk.length() > 0) {
            chunks.add(chunk.toString()); // Agregar el último chunk
        }
        return chunks;
    }

}
