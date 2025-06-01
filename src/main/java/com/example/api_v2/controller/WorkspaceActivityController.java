package com.example.api_v2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.api_v2.dto.WorkspaceActivityDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.service.WorkspaceActivityService;

import java.util.List;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceActivityController {

    private final WorkspaceActivityService workspaceActivityService;

    @GetMapping("/{workspaceId}/activity")
    public ResponseEntity<List<WorkspaceActivityDto>> getActivitiesByWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        log.info("Obteniendo actividades del workspace con ID: {}", workspaceId);
        
        // Validar el ID del workspace
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        List<WorkspaceActivityDto> activities = workspaceActivityService.getActivitiesByWorkspace(workspaceId);
        log.info("Se encontraron {} actividades para el workspace {}", activities.size(), workspaceId);
        return ResponseEntity.ok(activities);
    }
}
