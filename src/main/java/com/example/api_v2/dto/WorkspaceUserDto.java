package com.example.api_v2.dto;

import com.example.api_v2.model.WorkspaceUser;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkspaceUserDto {
    private Long id;
    private String userId;  // Identificador del usuario
    private String userEmail;  // Email del usuario
    private String firstName;  // Nombre del usuario
    private Long workspaceId;  // Identificador del workspace
    private String permissionType;  // Permiso del usuario (OWNER, MEMBER, etc.)
    private String profileImageUrl;  // Imagen de perfil del usuario

    public WorkspaceUserDto(Long id, String userId, String userEmail, String firstName, Long workspaceId, String permissionType, String profileImageUrl) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.firstName = firstName;
        this.workspaceId = workspaceId;
        this.permissionType = permissionType;
        this.profileImageUrl = profileImageUrl;
    }

    public static WorkspaceUserDto fromEntity(WorkspaceUser workspaceUser) {
        return new WorkspaceUserDto(
                workspaceUser.getId(),
                workspaceUser.getUser().getClerkId(),
                workspaceUser.getUser().getEmail(),
                workspaceUser.getUser().getFirstName(),
                workspaceUser.getWorkspace().getId(),
                workspaceUser.getPermissionType().name(),
                workspaceUser.getUser().getProfileImageUrl()
        );
    }
}
