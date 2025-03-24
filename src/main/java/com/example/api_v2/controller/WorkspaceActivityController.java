package com.example.api_v2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api_v2.dto.WorkspaceActivityDto;
import com.example.api_v2.service.WorkspaceActivityService;

import java.util.List;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceActivityController {

    private final WorkspaceActivityService workspaceActivityService;

    @GetMapping("/{workspaceId}/activity")
    public ResponseEntity<List<WorkspaceActivityDto>> getActivitiesByWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        return ResponseEntity.ok(workspaceActivityService.getActivitiesByWorkspace(workspaceId));
    }
}
