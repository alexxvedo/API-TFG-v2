package com.example.api_v2.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class UserActivityDto {
    private String type; // "achievement", "study", "streak", "collection"
    private String title;
    private String description;
    private String time;
    private Map<String, Object> stats; // Para estadísticas adicionales
    private LocalDateTime timestamp;
}
