package com.example.api_v2.dto;

import com.example.api_v2.model.KnowledgeLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

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
    private WorkspaceUserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}