package com.example.api_v2.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.example.api_v2.model.KnowledgeLevel;

@Data
public class UserFlashcardProgressDto {
    private Long id;
    private String userId;
    private Long flashcardId;
    private KnowledgeLevel knowledgeLevel;
    private Integer repetitionLevel;
    private Double easeFactor;
    private LocalDateTime nextReviewDate;
    private LocalDateTime lastReviewedAt;
    private Integer reviewCount;
    private Integer successCount;
    private Integer failureCount;
    private List<LocalDateTime> reviews;
    private String reviewResult;
    private Integer studyTimeInSeconds;
}
