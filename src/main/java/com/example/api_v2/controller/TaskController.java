package com.example.api_v2.controller;

import com.example.api_v2.dto.CreateTaskDto;
import com.example.api_v2.dto.TaskDto;
import com.example.api_v2.model.TaskStatus;
import com.example.api_v2.security.WorkspaceAccess;
import com.example.api_v2.security.WorkspaceEditAccess;
import com.example.api_v2.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        List<TaskDto> tasks = taskService.getTasksByWorkspace(workspaceId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("status/{status}")
    @WorkspaceAccess
    public ResponseEntity<List<TaskDto>> getTasksByWorkspaceAndStatus(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("status") String status) {
        log.info("Obteniendo tareas para el workspace: {} con estado: {}", workspaceId, status);
        TaskStatus taskStatus = TaskStatus.valueOf(status);
        List<TaskDto> tasks = taskService.getTasksByWorkspaceAndStatus(workspaceId, taskStatus);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<TaskDto>> getTasksByUser(@PathVariable("email") String email) {
        log.info("Obteniendo tareas para el usuario: {}", email);
        List<TaskDto> tasks = taskService.getTasksByUser(email);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @WorkspaceAccess
    public ResponseEntity<TaskDto> getTaskById(@PathVariable("taskId") Long taskId) {
        log.info("Obteniendo tarea por ID: {}", taskId);
        TaskDto task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/user/{createdById}")
    @WorkspaceEditAccess
    public ResponseEntity<TaskDto> createTask(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("createdById") String createdByEmail,
            @RequestBody CreateTaskDto createTaskDto) {
        log.info("Creando tarea en workspace {} por usuario {}: {}", workspaceId, createdByEmail, createTaskDto);
        TaskDto createdTask = taskService.createTask(workspaceId, createdByEmail, createTaskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PutMapping("/{taskId}")
    @WorkspaceEditAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("taskId") Long taskId,
            @RequestBody CreateTaskDto updateTaskDto) {
        log.info("Actualizando tarea {}: {}", taskId, updateTaskDto);
        TaskDto updatedTask = taskService.updateTask(taskId, updateTaskDto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @WorkspaceEditAccess(workspaceIdParam = "workspaceId")
    public ResponseEntity<Void> deleteTask(@PathVariable("workspaceId") Long workspaceId,
            @PathVariable("taskId") Long taskId) {
        log.info("Eliminando tarea: {}", taskId);
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
