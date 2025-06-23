package com.example.api_v2.service;

import com.example.api_v2.dto.CreateSubtaskDto;
import com.example.api_v2.dto.CreateTaskDto;
import com.example.api_v2.dto.TaskDto;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.*;
import com.example.api_v2.repository.SubtaskRepository;
import com.example.api_v2.repository.TaskRepository;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceActivityService workspaceActivityService;

    public TaskService(TaskRepository taskRepository, SubtaskRepository subtaskRepository,
            WorkspaceRepository workspaceRepository, UserRepository userRepository, 
            WorkspaceActivityService workspaceActivityService) {
        this.taskRepository = taskRepository;
        this.subtaskRepository = subtaskRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.workspaceActivityService = workspaceActivityService;
    }

    public List<TaskDto> getTasksByWorkspace(Long workspaceId) {
        return taskRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::convertToTaskDtoWithSubtasks)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByWorkspaceAndStatus(Long workspaceId, TaskStatus status) {
        return taskRepository.findByWorkspaceIdAndStatus(workspaceId, status).stream()
                .map(this::convertToTaskDtoWithSubtasks)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return taskRepository.findByAssignedToId(user.getId()).stream()
                .map(this::convertToTaskDtoWithSubtasks)
                .collect(Collectors.toList());
    }

    public TaskDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return convertToTaskDtoWithSubtasks(task);
    }

    @Transactional
    public TaskDto createTask(Long workspaceId, String createdByEmail, CreateTaskDto createTaskDto) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));

        User createdBy = userRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + createdByEmail));

        User assignedTo = null;
        if (createTaskDto.getAssignedToId() != null) {
            assignedTo = userRepository.findById(createTaskDto.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + createTaskDto.getAssignedToId()));
        }

        Task task = new Task();
        task.setTitle(createTaskDto.getTitle());
        task.setDescription(createTaskDto.getDescription());

        if (createTaskDto.getPriority() != null) {
            task.setPriority(TaskPriority.valueOf(createTaskDto.getPriority()));
        }

        if (createTaskDto.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(createTaskDto.getStatus()));
        }

        task.setDueDate(createTaskDto.getDueDate());
        task.setWorkspace(workspace);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);

        Task savedTask = taskRepository.save(task);

        // Registrar la actividad
        workspaceActivityService.logTaskCreated(workspaceId, createdByEmail, createTaskDto.getTitle());

        // Crear subtareas si existen
        if (createTaskDto.getSubtasks() != null && !createTaskDto.getSubtasks().isEmpty()) {
            for (CreateSubtaskDto subtaskDto : createTaskDto.getSubtasks()) {
                Subtask subtask = new Subtask();
                subtask.setTitle(subtaskDto.getTitle());
                subtask.setCompleted(subtaskDto.isCompleted());
                subtask.setTask(savedTask);
                subtaskRepository.save(subtask);
            }
        }

        return convertToTaskDtoWithSubtasks(savedTask);
    }

    @Transactional
    public TaskDto updateTask(Long taskId, CreateTaskDto updateTaskDto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.setTitle(updateTaskDto.getTitle());
        task.setDescription(updateTaskDto.getDescription());

        if (updateTaskDto.getPriority() != null) {
            task.setPriority(TaskPriority.valueOf(updateTaskDto.getPriority()));
        }

        if (updateTaskDto.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(updateTaskDto.getStatus()));
        }

        task.setDueDate(updateTaskDto.getDueDate());

        if (updateTaskDto.getAssignedToId() != null) {
            User assignedTo = userRepository.findByEmail(updateTaskDto.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with email: " + updateTaskDto.getAssignedToId()));
            task.setAssignedTo(assignedTo);
        } else {
            task.setAssignedTo(null);
        }

        Task updatedTask = taskRepository.save(task);

        // Registrar actividad si la tarea fue completada
        if (updateTaskDto.getStatus() != null && 
            TaskStatus.valueOf(updateTaskDto.getStatus()) == TaskStatus.DONE &&
            task.getStatus() != TaskStatus.DONE) {
            
            // Encontrar el email del usuario que está actualizando
            // Necesitaríamos pasarlo como parámetro, por ahora usar el creador
            String userEmail = task.getCreatedBy().getEmail();
            workspaceActivityService.logTaskCompleted(
                task.getWorkspace().getId(), 
                userEmail, 
                task.getTitle()
            );
        }

        // Actualizar subtareas
        if (updateTaskDto.getSubtasks() != null) {
            // Eliminar subtareas existentes
            subtaskRepository.deleteByTaskId(taskId);

            // Crear nuevas subtareas
            for (CreateSubtaskDto subtaskDto : updateTaskDto.getSubtasks()) {
                Subtask subtask = new Subtask();
                subtask.setTitle(subtaskDto.getTitle());
                subtask.setCompleted(subtaskDto.isCompleted());
                subtask.setTask(updatedTask);
                subtaskRepository.save(subtask);
            }
        }

        return convertToTaskDtoWithSubtasks(updatedTask);
    }

    @Transactional
    public void deleteTask(Long taskId, String userEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Registrar la actividad antes de eliminar
        workspaceActivityService.logTaskDeleted(
            task.getWorkspace().getId(), 
            userEmail, 
            task.getTitle()
        );

        // Las subtareas se eliminarán automáticamente debido a CascadeType.ALL
        taskRepository.deleteById(taskId);
    }

    private TaskDto convertToTaskDtoWithSubtasks(Task task) {
        TaskDto taskDto = task.toDto();

        // Obtener y agregar subtareas
        List<Subtask> subtasks = subtaskRepository.findByTaskId(task.getId());
        taskDto.setSubtasks(subtasks.stream()
                .map(Subtask::toDto)
                .collect(Collectors.toList()));

        return taskDto;
    }
}
