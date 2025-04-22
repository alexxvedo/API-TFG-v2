package com.example.api_v2.model;

import com.example.api_v2.dto.TaskDto;
import com.example.api_v2.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @JsonBackReference(value = "workspace-tasks")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtask> subtasks = new ArrayList<>();

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

    // Evitar problemas con ciclos infinitos
    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', status=" + status + "}";
    }

    public TaskDto toDto() {
        TaskDto dto = new TaskDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setPriority(priority.name());
        dto.setStatus(status.name());
        dto.setDueDate(dueDate);
        dto.setWorkspaceId(workspace != null ? workspace.getId() : null);
        dto.setAssignedToId(assignedTo != null ? assignedTo.getEmail() : null);
        dto.setCreatedById(createdBy != null ? createdBy.getEmail() : null);
        
        // Añadir información del usuario asignado si existe
        if (assignedTo != null) {
            UserDto assignedToUserDto = new UserDto();
            assignedToUserDto.setId(assignedTo.getId());
            assignedToUserDto.setName(assignedTo.getName());
            assignedToUserDto.setEmail(assignedTo.getEmail());
            assignedToUserDto.setImage(assignedTo.getImage());
            dto.setAssignedToUser(assignedToUserDto);
        }
        
        // Añadir información del usuario creador si existe
        if (createdBy != null) {
            UserDto createdByUserDto = new UserDto();
            createdByUserDto.setId(createdBy.getId());
            createdByUserDto.setName(createdBy.getName());
            createdByUserDto.setEmail(createdBy.getEmail());
            createdByUserDto.setImage(createdBy.getImage());
            dto.setCreatedByUser(createdByUserDto);
        }
        
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }
}
