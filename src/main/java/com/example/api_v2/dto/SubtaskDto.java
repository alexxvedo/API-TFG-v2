package com.example.api_v2.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubtaskDto {
    private Long id;
    private String title;
    private boolean completed;
    private Long taskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
