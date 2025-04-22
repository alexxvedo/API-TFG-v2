package com.example.api_v2.model;

import com.example.api_v2.dto.SubtaskDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "subtasks")
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonBackReference(value = "task-subtasks")
    private Task task;

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
        return "Subtask{id=" + id + ", title='" + title + "', completed=" + completed + "}";
    }

    public SubtaskDto toDto() {
        SubtaskDto dto = new SubtaskDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setCompleted(completed);
        dto.setTaskId(task != null ? task.getId() : null);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }
}
