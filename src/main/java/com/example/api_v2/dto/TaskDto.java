package com.example.api_v2.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private String priority; // "LOW", "MEDIUM", "HIGH"
    private String status; // "TODO", "IN_PROGRESS", "DONE"
    private LocalDate dueDate;
    private Long workspaceId;
    private String assignedToId;
    private String createdById;
    private List<SubtaskDto> subtasks = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Información adicional sobre el usuario asignado
    private UserDto assignedToUser;
    // Información adicional sobre el usuario creador
    private UserDto createdByUser;
}
