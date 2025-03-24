package com.example.api_v2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.example.api_v2.dto.WorkspaceActivityDto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "workspace_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @JsonBackReference(value = "workspace-activities")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-activities")
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public WorkspaceActivityDto toDto() {
        WorkspaceActivityDto dto = new WorkspaceActivityDto();
        dto.setId(id);
        dto.setWorkspace(workspace.toDto());
        dto.setUser(user.toDto());
        dto.setAction(action);
        dto.setTimestamp(timestamp);
        return dto;
    }
}