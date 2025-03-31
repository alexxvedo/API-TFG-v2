package com.example.api_v2.model;

import com.example.api_v2.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"User\"")
@Data
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "\"emailVerified\"")
    private LocalDateTime emailVerified;

    @Column(name = "image")
    private String image;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-accounts")
    private List<Account> accounts = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-workspaces")
    @JsonIgnore
    private List<WorkspaceUser> workspaceUsers = new ArrayList<>();


    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-collections")
    @JsonIgnore
    private List<Collection> collections;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-flashcards")
    @JsonIgnore
    private List<Flashcard> flashcards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-study-sessions")
    @JsonIgnore
    private List<StudySession> studySessions;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-notes")
    @JsonIgnore
    private List<Note> notes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-activities")
    private List<WorkspaceActivity> activities;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-stats")
    private UserStats userStats;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"updatedAt\"", insertable = false, updatable = false)
    private LocalDateTime updatedAt; 


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (userStats == null) {
            UserStats stats = new UserStats();
            stats.setUser(this);
            stats.setCreatedFlashcards(0);
            stats.setStudySeconds(0);
            stats.setStudiedFlashcards(0);
            stats.setLevel(1);
            stats.setExperience(0);
            stats.setExperienceToNextLevel(100);
            stats.setTotalExperience(0);
            stats.setCreatedAt(LocalDateTime.now());
            stats.setUpdatedAt(LocalDateTime.now());
            this.userStats = stats;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UserDto toDto() {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        dto.setImage(image);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }
}
