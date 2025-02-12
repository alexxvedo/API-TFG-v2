package com.example.api_v2.dto;

import com.example.api_v2.model.Flashcard;
import com.example.api_v2.model.WorkspaceUser;
import lombok.Data;

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
    private List<Flashcard> flashcards;
    private WorkspaceUser createdBy;
}
