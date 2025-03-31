package com.example.api_v2.model;

import com.example.api_v2.dto.UserStatsDto;
import com.example.api_v2.dto.WorkspaceDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Getter
@Setter
@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-stats")
    private User user;

    // Estadísticas Generales
    @Column(name = "created_flashcards", nullable = false)
    private int createdFlashcards;

    @Column(name = "study_seconds", nullable = false)
    private int studySeconds;

    @Column(name = "studied_flashcards", nullable = false)
    private int studiedFlashcards;

    // Sistema de Nivel
    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "experience", nullable = false)
    private int experience;

    @Column(name = "experience_to_next_level", nullable = false)
    private int experienceToNextLevel;

    @Column(name = "total_experience", nullable = false)
    private int totalExperience;

    // Logros
    @Column(name = "unlocked_achievements")
    @ElementCollection
    private List<String> unlockedAchievements = new ArrayList<>();

    @Column(name = "achievement_progress")
    @ElementCollection
    private Map<String, Integer> achievementProgress = new HashMap<>();

    @Column(name = "total_achievements")
    private int totalAchievements;

    // Actividad
    @Column(name = "last_active_date")
    private LocalDateTime lastActiveDate;

    @Column(name = "daily_streak")
    private int dailyStreak;

    @Column(name = "total_active_days")
    private int totalActiveDays;

    // Colecciones
    @Column(name = "total_collections")
    private int totalCollections;

    @Column(name = "active_collections")
    private int activeCollections;

    @Column(name = "flashcards_per_collection")
    private double flashcardsPerCollection;

    @Column(name = "study_time_per_collection")
    private int studyTimePerCollection;

    // Workspaces
    @Column(name = "total_workspaces")
    private int totalWorkspaces;

    @Column(name = "active_workspaces")
    private int activeWorkspaces;

    @Column(name = "workspace_contributions")
    @ElementCollection
    private Map<String, Integer> workspaceContributions = new HashMap<>();

    // Métricas de Rendimiento
    @Column(name = "average_accuracy")
    private double averageAccuracy;

    @Column(name = "best_subjects")
    @ElementCollection
    private List<String> bestSubjects = new ArrayList<>();

    @Column(name = "study_sessions_completed")
    private int studySessionsCompleted;

    @Column(name = "average_session_duration")
    private int averageSessionDuration;

    // Métricas Sociales
    @Column(name = "collaborations")
    private int collaborations;

    @Column(name = "helpful_ratings")
    private int helpfulRatings;

    @Column(name = "social_interactions")
    private int socialInteractions;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Inicializar valores por defecto
        level = 1;
        experienceToNextLevel = 100;
        totalAchievements = 0;
        dailyStreak = 0;
        totalActiveDays = 0;
        lastActiveDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastActiveDate = LocalDateTime.now();
    }

    // 
    @Override
    public String toString() {
        return "UserStats{id=" + id + 
               ", createdFlashcards=" + createdFlashcards + 
               ", studySeconds=" + studySeconds + 
               ", studiedFlashcards=" + studiedFlashcards + 
               ", level=" + level +
               ", experience=" + experience +
               ", totalExperience=" + totalExperience + "}";
    }

    public UserStatsDto toDto() {
        UserStatsDto dto = new UserStatsDto();
        dto.setId(id);
        dto.setCreatedAt(createdAt.toString());
        dto.setUpdatedAt(updatedAt.toString());
        dto.setUser(user.toDto());
        
        // Estadísticas Generales
        dto.setCreatedFlashcards(createdFlashcards);
        dto.setStudySeconds(studySeconds);
        dto.setStudiedFlashcards(studiedFlashcards);
        
        // Sistema de Nivel
        dto.setLevel(level);
        dto.setExperience(experience);
        dto.setExperienceToNextLevel(experienceToNextLevel);
        dto.setTotalExperience(totalExperience);
        
        // Logros
        dto.setUnlockedAchievements(unlockedAchievements);
        dto.setAchievementProgress(achievementProgress);
        dto.setTotalAchievements(totalAchievements);
        
        // Actividad
        dto.setLastActiveDate(lastActiveDate.toString());
        dto.setDailyStreak(dailyStreak);
        dto.setTotalActiveDays(totalActiveDays);
        
        // Colecciones
        dto.setTotalCollections(totalCollections);
        dto.setActiveCollections(activeCollections);
        dto.setFlashcardsPerCollection(flashcardsPerCollection);
        dto.setStudyTimePerCollection(studyTimePerCollection);
        
        // Workspaces
        dto.setTotalWorkspaces(totalWorkspaces);
        dto.setActiveWorkspaces(activeWorkspaces);
        dto.setWorkspaceContributions(workspaceContributions);
        
        // Métricas de Rendimiento
        dto.setAverageAccuracy(averageAccuracy);
        dto.setBestSubjects(bestSubjects);
        dto.setStudySessionsCompleted(studySessionsCompleted);
        dto.setAverageSessionDuration(averageSessionDuration);
        
        // Métricas Sociales
        dto.setCollaborations(collaborations);
        dto.setHelpfulRatings(helpfulRatings);
        dto.setSocialInteractions(socialInteractions);
        
        return dto;
    }
}
