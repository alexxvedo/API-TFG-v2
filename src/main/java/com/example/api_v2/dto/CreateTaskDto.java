package com.example.api_v2.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateTaskDto {
    private String title;
    private String description;
    private String priority; // "LOW", "MEDIUM", "HIGH"
    private String status; // "TODO", "IN_PROGRESS", "DONE"
    private LocalDate dueDate;
    private String assignedToId;
    private List<CreateSubtaskDto> subtasks = new ArrayList<>();
}
