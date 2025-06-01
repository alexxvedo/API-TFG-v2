package com.example.api_v2.controller;

import com.example.api_v2.dto.CreateTaskDto;
import com.example.api_v2.dto.TaskDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.model.TaskStatus;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/workspace/{workspaceId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/")
    @WorkspaceAccess
    public ResponseEntity<List<TaskDto>> getTasksByWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        log.info("Obteniendo tareas para el workspace: {}", workspaceId);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        List<TaskDto> tasks = taskService.getTasksByWorkspace(workspaceId);
        log.info("Se encontraron {} tareas para el workspace {}", tasks.size(), workspaceId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("status/{status}")
    @WorkspaceAccess
    public ResponseEntity<List<TaskDto>> getTasksByWorkspaceAndStatus(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("status") String status) {
        log.info("Obteniendo tareas para el workspace: {} con estado: {}", workspaceId, status);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (status == null || status.trim().isEmpty()) {
            log.error("Estado de tarea inválido: {}", status);
            ErrorUtils.throwValidationError("El estado de la tarea es obligatorio");
        }
        
        try {
            TaskStatus taskStatus = TaskStatus.valueOf(status);
            List<TaskDto> tasks = taskService.getTasksByWorkspaceAndStatus(workspaceId, taskStatus);
            log.info("Se encontraron {} tareas para el workspace {} con estado {}", tasks.size(), workspaceId, status);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            log.error("Estado de tarea no válido: {}", status);
            ErrorUtils.throwValidationError("El estado '" + status + "' no es válido. Estados válidos: " + 
                                          java.util.Arrays.toString(TaskStatus.values()));
            return null; // Este return nunca se ejecutará, pero es necesario para que compile
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<TaskDto>> getTasksByUser(@PathVariable("email") String email) {
        log.info("Obteniendo tareas para el usuario: {}", email);
        
        // Validar los parámetros de entrada
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        List<TaskDto> tasks = taskService.getTasksByUser(email);
        log.info("Se encontraron {} tareas para el usuario {}", tasks.size(), email);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @WorkspaceAccess
    public ResponseEntity<TaskDto> getTaskById(@PathVariable("taskId") Long taskId) {
        log.info("Obteniendo tarea por ID: {}", taskId);
        
        // Validar los parámetros de entrada
        if (taskId == null || taskId <= 0) {
            log.error("ID de tarea inválido: {}", taskId);
            ErrorUtils.throwValidationError("El ID de la tarea debe ser un número positivo");
        }
        
        TaskDto task = taskService.getTaskById(taskId);
        log.info("Tarea obtenida: {}", task);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/user/{createdById}")
    @WorkspaceEditAccess
    public ResponseEntity<TaskDto> createTask(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("createdById") String createdByEmail,
            @RequestBody CreateTaskDto createTaskDto) {
        log.info("Creando tarea en workspace {} por usuario {}: {}", workspaceId, createdByEmail, createTaskDto);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (createdByEmail == null || createdByEmail.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", createdByEmail);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        // Validar el DTO de la tarea
        if (createTaskDto == null) {
            log.error("Datos de tarea no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la tarea son obligatorios");
        }
        
        // Ya que hemos verificado que createTaskDto no es nulo, podemos acceder a sus propiedades
        if (createTaskDto != null) {
            if (createTaskDto.getTitle() == null || createTaskDto.getTitle().trim().isEmpty()) {
                log.error("Título de tarea no proporcionado");
                ErrorUtils.throwValidationError("El título de la tarea es obligatorio");
            }
        }
        
        TaskDto createdTask = taskService.createTask(workspaceId, createdByEmail, createTaskDto);
        log.info("Tarea creada con ID: {}", createdTask.getId());
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{taskId}")
    @WorkspaceEditAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("taskId") Long taskId,
            @RequestBody CreateTaskDto updateTaskDto) {
        log.info("Actualizando tarea {}: {}", taskId, updateTaskDto);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (taskId == null || taskId <= 0) {
            log.error("ID de tarea inválido: {}", taskId);
            ErrorUtils.throwValidationError("El ID de la tarea debe ser un número positivo");
        }
        
        // Validar el DTO de la tarea
        if (updateTaskDto == null) {
            log.error("Datos de tarea no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la tarea son obligatorios");
        }
        
        // Ya que hemos verificado que updateTaskDto no es nulo, podemos acceder a sus propiedades
        if (updateTaskDto != null) {
            if (updateTaskDto.getTitle() == null || updateTaskDto.getTitle().trim().isEmpty()) {
                log.error("Título de tarea no proporcionado");
                ErrorUtils.throwValidationError("El título de la tarea es obligatorio");
            }
        }
        
        TaskDto updatedTask = taskService.updateTask(taskId, updateTaskDto);
        log.info("Tarea actualizada: {}", updatedTask);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @WorkspaceEditAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<Void> deleteTask(@PathVariable("workspaceId") Long workspaceId,
            @PathVariable("taskId") Long taskId) {
        log.info("Eliminando tarea: {}", taskId);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        if (taskId == null || taskId <= 0) {
            log.error("ID de tarea inválido: {}", taskId);
            ErrorUtils.throwValidationError("El ID de la tarea debe ser un número positivo");
        }
        
        taskService.deleteTask(taskId);
        log.info("Tarea {} eliminada correctamente", taskId);
        return ResponseEntity.noContent().build();
    }
}
