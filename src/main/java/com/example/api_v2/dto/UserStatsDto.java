package com.example.api_v2.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UserStatsDto {
    private Long id;
    private String createdAt;
    private String updatedAt;
    private UserDto user;

    // Estadísticas Generales
    private Integer createdFlashcards;
    private Integer studySeconds;
    private Integer studiedFlashcards;

    // Sistema de Nivel
    private Integer level;
    private Integer experience;
    private Integer experienceToNextLevel;
    private Integer totalExperience;

    // Logros
    private List<String> unlockedAchievements;
    private Map<String, Integer> achievementProgress;
    private Integer totalAchievements;

    // Actividad
    private String lastActiveDate;
    private Integer dailyStreak;
    private Integer totalActiveDays;

    // Colecciones
    private Integer totalCollections;
    private Integer activeCollections;
    private Double flashcardsPerCollection;
    private Integer studyTimePerCollection;

    // Workspaces
    private Integer totalWorkspaces;
    private Integer activeWorkspaces;
    private Map<String, Integer> workspaceContributions;

    // Métricas de Rendimiento
    private Double averageAccuracy;
    private List<String> bestSubjects;
    private Integer studySessionsCompleted;
    private Integer averageSessionDuration;

    // Métricas Sociales
    private Integer collaborations;
    private Integer helpfulRatings;
    private Integer socialInteractions;
    
    // Estadísticas diarias
    private Map<String, Object> todayStats;
}