package com.example.api_v2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardReviewDto {
    private Long flashcardId;
    private String result;  // WRONG, PARTIAL, CORRECT
    private Long timeSpentMs;  // Tiempo dedicado a la tarjeta en milisegundos
    private String userId;  // ID del usuario que realiza la revisión
}