package com.example.api_v2.dto;

import com.example.api_v2.model.KnowledgeLevel;
import com.example.api_v2.model.UserFlashcardProgress;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardDto {
    private Long id;
    private String question;
    private String answer;
    private Long collectionId;
    private KnowledgeLevel knowledgeLevel;
    private Integer repetitionLevel;
    private LocalDateTime nextReviewDate;
    private LocalDateTime lastReviewedAt;
    private String status;
    private String notes;
    private String tags;
    private UserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserFlashcardProgressDto> userFlashcardProgress;
}