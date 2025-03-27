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

    @Column(name = "created_flashcards", nullable = false)
    private int createdFlashcards;

    @Column(name = "study_seconds", nullable = false)
    private int studySeconds;

    @Column(name = "studied_flashcards", nullable = false)
    private int studiedFlashcards;

    @Column(name = "exp_level", nullable = false)
    private int expLevel;

    @Column(name = "current_level_exp", nullable = false)
    private int currentLevelExp;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 🔹 Evitar problemas con ciclos infinitos
    @Override
    public String toString() {
        return "UserStats{id=" + id + ", user=" + user + ", createdFlashcards=" + createdFlashcards + ", studySeconds=" + studySeconds + ", studiedFlashcards=" + studiedFlashcards + ", expLevel=" + expLevel + ", currentLevelExp=" + currentLevelExp + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }

    public UserStatsDto toDto() {
        UserStatsDto dto = new UserStatsDto();
        dto.setId(id);
        dto.setCreatedAt(createdAt.toString());
        dto.setUpdatedAt(updatedAt.toString());
        dto.setUser(user.toDto());
        dto.setCreatedFlashcards(createdFlashcards);
        dto.setStudySeconds(studySeconds);
        dto.setStudiedFlashcards(studiedFlashcards);
        dto.setExpLevel(expLevel);
        dto.setCurrentLevelExp(currentLevelExp);
        return dto;
    }
}
