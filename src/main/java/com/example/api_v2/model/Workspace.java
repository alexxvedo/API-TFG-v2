package com.example.api_v2.model;


import com.example.api_v2.dto.WorkspaceDto;
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
@Table(name = "workspaces")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceUser> workspaceUsers = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "workspace-collections")
    private List<Collection> collections = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "workspace-activities")
    private List<WorkspaceActivity> activities = new ArrayList<>();

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
        return "Workspace{id=" + id + ", name='" + name + "', description='" + description + "'}";
    }

    public WorkspaceDto toDto() {
        WorkspaceDto dto = new WorkspaceDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }
}
