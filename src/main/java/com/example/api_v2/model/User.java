package com.example.api_v2.model;

import com.example.api_v2.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
    private List<WorkspaceUser> workspaceUsers = new ArrayList<>();


    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-collections")
    private List<Collection> collections;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-flashcards")
    private List<Flashcard> flashcards;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-notes")
    private List<Note> notes;

    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"updatedAt\"", insertable = false, updatable = false)
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
