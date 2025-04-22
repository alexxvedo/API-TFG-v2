package com.example.api_v2.controller;

import com.example.api_v2.dto.CreateTaskDto;
import com.example.api_v2.dto.TaskDto;
import com.example.api_v2.model.TaskStatus;
import com.example.api_v2.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<TaskDto>> getTasksByWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        List<TaskDto> tasks = taskService.getTasksByWorkspace(workspaceId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/workspace/{workspaceId}/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByWorkspaceAndStatus(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("status") String status) {
        TaskStatus taskStatus = TaskStatus.valueOf(status);
        List<TaskDto> tasks = taskService.getTasksByWorkspaceAndStatus(workspaceId, taskStatus);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<TaskDto>> getTasksByUser(@PathVariable("email") String email) {
        List<TaskDto> tasks = taskService.getTasksByUser(email);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable("taskId") Long taskId) {
        TaskDto task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/workspace/{workspaceId}/user/{createdById}")
    public ResponseEntity<TaskDto> createTask(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("createdById") String createdByEmail,
            @RequestBody CreateTaskDto createTaskDto) {
        TaskDto createdTask = taskService.createTask(workspaceId, createdByEmail, createTaskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable("taskId") Long taskId,
            @RequestBody CreateTaskDto updateTaskDto) {
        TaskDto updatedTask = taskService.updateTask(taskId, updateTaskDto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable("taskId") Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
