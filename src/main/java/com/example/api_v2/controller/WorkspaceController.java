package com.example.api_v2.controller;

import com.example.api_v2.dto.UserDto;
import com.example.api_v2.dto.WorkspaceDto;
import com.example.api_v2.model.PermissionType;
import com.example.api_v2.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/user/{email}")
    public ResponseEntity<List<WorkspaceDto>> getWorkspacesByUserId(@PathVariable String email) {
        List<WorkspaceDto> workspaces = workspaceService.getWorkspacesByUserEmail(email);
        log.info("Returning {} workspaces for user {}", workspaces.size(), email);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceDto> getWorkspace(@PathVariable Long id) {
        WorkspaceDto workspace = workspaceService.getWorkspace(id);
        log.info("Returning workspace: {}", workspace);
        return ResponseEntity.ok(workspace);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserDto>> getWorkspaceUsers(@PathVariable Long id) {
        List<UserDto> workspaceUsers = workspaceService.getWorkspaceUsers(id);
        log.info("Returning {} workspace users for workspace {}", workspaceUsers.size(), id);
        return ResponseEntity.ok(workspaceUsers);
    }

    @PostMapping("/user/{email}")
    public ResponseEntity<WorkspaceDto> createWorkspace(
            @PathVariable String email,
            @RequestBody WorkspaceDto workspaceDto) {
        log.info("Creating workspace for user {}: {}", email, workspaceDto);
        return ResponseEntity.ok(workspaceService.createWorkspace(workspaceDto, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceDto> updateWorkspace(
            @PathVariable Long id,
            @RequestBody WorkspaceDto workspaceDto) {
        log.info("Updating workspace {}: {}", id, workspaceDto);
        return ResponseEntity.ok(workspaceService.updateWorkspace(id, workspaceDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
        log.info("Deleting workspace: {}", id);
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.ok().build();
    }

    

    @PostMapping("/{id}/join/{email}/{permissionType}")
    public ResponseEntity<Void> joinWorkspace(@PathVariable Long id, @PathVariable String email, @PathVariable PermissionType permissionType) {
        log.info("Joining workspace {} by user {}", id, email);
        workspaceService.joinWorkspace(id, email, permissionType);
        return ResponseEntity.ok().build();
    }
}