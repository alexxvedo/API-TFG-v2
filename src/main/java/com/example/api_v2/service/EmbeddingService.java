package com.example.api_v2.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final WebClient webClient;

    public EmbeddingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8000").build();
    }

    public Mono<List<Float>> getEmbeddings(String text) {
        Map<String, String> requestBody = Map.of("question", text);

        // Utilizamos float[] para la deserialización y luego lo convertimos a List<Float>
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/generate_embedding/")
                    .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(float[].class)
                .map(array -> {
                    List<Float> list = new ArrayList<>(array.length);
                    for (float value : array) {
                        list.add(value);
                    }
                    return list;
                });
    }
}
