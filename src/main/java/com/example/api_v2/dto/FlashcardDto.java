package com.example.api_v2.dto;

import com.example.api_v2.model.KnowledgeLevel;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardDto {
    private Long id;
    private String question;
    private String answer;
    private String difficulty;
    private Long collectionId;
    private KnowledgeLevel knowledgeLevel;
    private LocalDateTime nextReviewDate;
    private LocalDateTime lastReviewedAt;
    private String status;
    private Integer repetitionLevel;
    private Double easeFactor;
    private List<LocalDateTime> reviews;
    private Integer reviewCount;
    private Integer successCount;
    private Integer failureCount;
    private Integer studyTimeInSeconds;
    private UserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}