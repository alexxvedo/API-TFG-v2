package com.example.api_v2.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudySessionDto {
    private Long id;
    private Long collectionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;
    private int totalCards;
    private int correctAnswers;
    private int incorrectAnswers;
    private List<FlashcardDto> flashcards;
    private UserDto user;
    private String mode;

    @Data
    public static class FlashcardActivityDto {
        private Long id;
        private Long flashcardId;
        private boolean correct;
        private LocalDateTime timestamp;
        private String newStatus;
        private String userAnswer;
    }
}
