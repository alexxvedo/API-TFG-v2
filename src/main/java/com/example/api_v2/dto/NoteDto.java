package com.example.api_v2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {
    private Long id;
    private Long collectionId;
    private String noteName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private WorkspaceUserDto createdBy;
}