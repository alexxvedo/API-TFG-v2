package com.example.api_v2.controller;

import com.example.api_v2.dto.UserDto;
import com.example.api_v2.dto.WorkspaceDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.model.PermissionType;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceOwnerAccess;
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
    public ResponseEntity<List<WorkspaceDto>> getWorkspacesByUserId(@PathVariable("email") String email) {
        log.info("Obteniendo workspaces para el usuario: {}", email);
        
        // Validar los parámetros de entrada
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        List<WorkspaceDto> workspaces = workspaceService.getWorkspacesByUserEmail(email);
        log.info("Returning {} workspaces for user {}", workspaces.size(), email);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    @WorkspaceAccess(workspaceIdParam = "id")
    public ResponseEntity<WorkspaceDto> getWorkspace(@PathVariable("id") Long id) {
        log.info("Obteniendo workspace con id: {}", id);
        
        // Validar los parámetros de entrada
        if (id == null || id <= 0) {
            log.error("ID de workspace inválido: {}", id);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        WorkspaceDto workspace = workspaceService.getWorkspace(id);
        log.info("Returning workspace: {}", workspace);
        return ResponseEntity.ok(workspace);
    }

    @GetMapping("/{id}/users")
    @WorkspaceAccess(workspaceIdParam = "id")
    public ResponseEntity<List<UserDto>> getWorkspaceUsers(@PathVariable("id") Long id) {
        log.info("Obteniendo usuarios del workspace con id: {}", id);
        
        // Validar los parámetros de entrada
        if (id == null || id <= 0) {
            log.error("ID de workspace inválido: {}", id);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        List<UserDto> workspaceUsers = workspaceService.getWorkspaceUsers(id);
        log.info("Returning {} workspace users for workspace {}", workspaceUsers.size(), id);
        return ResponseEntity.ok(workspaceUsers);
    }

    @PostMapping(value = "/user/{email}")
    public ResponseEntity<WorkspaceDto> createWorkspace(
            @PathVariable("email") String email,
            @RequestBody WorkspaceDto workspaceDto) {
        log.info("Creando workspace para el usuario {}: {}", email, workspaceDto);
        
        // Validar los parámetros de entrada
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        // Validar el DTO del workspace
        if (workspaceDto == null) {
            log.error("Datos de workspace no proporcionados");
            ErrorUtils.throwValidationError("Los datos del workspace son obligatorios");
        }
        
        // Ya que hemos verificado que workspaceDto no es nulo, podemos acceder a sus propiedades
        if (workspaceDto != null && (workspaceDto.getName() == null || workspaceDto.getName().trim().isEmpty())) {
            log.error("Nombre de workspace no proporcionado");
            ErrorUtils.throwValidationError("El nombre del workspace es obligatorio");
        }
        
        return ResponseEntity.ok(workspaceService.createWorkspace(workspaceDto, email));
    }

    @PutMapping("/{id}")
    @WorkspaceOwnerAccess(workspaceIdParam = "id")
    public ResponseEntity<WorkspaceDto> updateWorkspace(
            @PathVariable("id") Long id,
            @RequestBody WorkspaceDto workspaceDto) {
        log.info("Actualizando workspace {}: {}", id, workspaceDto);
        
        // Validar los parámetros de entrada
        if (id == null || id <= 0) {
            log.error("ID de workspace inválido: {}", id);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        // Validar el DTO del workspace
        if (workspaceDto == null) {
            log.error("Datos de workspace no proporcionados");
            ErrorUtils.throwValidationError("Los datos del workspace son obligatorios");
        }
        
        // Ya que hemos verificado que workspaceDto no es nulo, podemos acceder a sus propiedades
        if (workspaceDto != null && (workspaceDto.getName() == null || workspaceDto.getName().trim().isEmpty())) {
            log.error("Nombre de workspace no proporcionado");
            ErrorUtils.throwValidationError("El nombre del workspace es obligatorio");
        }
        
        return ResponseEntity.ok(workspaceService.updateWorkspace(id, workspaceDto));
    }

    @DeleteMapping("/{id}")
    @WorkspaceOwnerAccess(workspaceIdParam = "id")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable("id") Long id) {
        log.info("Eliminando workspace: {}", id);
        
        // Validar los parámetros de entrada
        if (id == null || id <= 0) {
            log.error("ID de workspace inválido: {}", id);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join/{email}/{permissionType}")
    @WorkspaceOwnerAccess(workspaceIdParam = "id")
    public ResponseEntity<Void> joinWorkspace(@PathVariable("id") Long id, @PathVariable("email") String email,
            @PathVariable("permissionType") PermissionType permissionType) {
        log.info("Usuario {} uniéndose al workspace {} con permiso {}", email, id, permissionType);
        
        // Validar los parámetros de entrada
        if (id == null || id <= 0) {
            log.error("ID de workspace inválido: {}", id);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        if (permissionType == null) {
            log.error("Tipo de permiso no proporcionado");
            ErrorUtils.throwValidationError("El tipo de permiso es obligatorio");
        }
        
        workspaceService.joinWorkspace(id, email, permissionType);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/join-by-invite/{id}/{email}/{permissionType}")
    public ResponseEntity<Void> joinWorkspaceByInvite(
            @PathVariable("id") Long id, 
            @PathVariable("email") String email,
            @PathVariable("permissionType") PermissionType permissionType) {
        log.info("Usuario {} uniéndose al workspace {} a través de invitación con permiso {}", email, id, permissionType);
        
        // Validar los parámetros de entrada
        if (id == null || id <= 0) {
            log.error("ID de workspace inválido: {}", id);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        if (permissionType == null) {
            log.error("Tipo de permiso no proporcionado");
            ErrorUtils.throwValidationError("El tipo de permiso es obligatorio");
        }
        
        workspaceService.joinWorkspace(id, email, permissionType);
        return ResponseEntity.noContent().build();
    }
}