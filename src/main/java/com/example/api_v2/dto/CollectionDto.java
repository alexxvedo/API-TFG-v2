package com.example.api_v2.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CollectionDto {
    // Setters
    // Getters
    private Long id;
    private String name;
    private String description;
    private Long workspaceId;
    private Integer itemCount;
    private List<FlashcardDto> flashcards;
    private UserDto createdBy;
    private LocalDateTime createdAt;
}
