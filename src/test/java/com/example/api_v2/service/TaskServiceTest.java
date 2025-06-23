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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TaskService
 * Valida la lógica de negocio relacionada con gestión de tareas
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private User assignedUser;
    private Workspace testWorkspace;
    private Task testTask;
    private Subtask testSubtask;
    private CreateTaskDto createTaskDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        assignedUser = new User();
        assignedUser.setId("assigned-user-id");
        assignedUser.setEmail("assigned@example.com");
        assignedUser.setName("Assigned User");

        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setStatus(TaskStatus.TODO);
        testTask.setDueDate(LocalDate.now().plusDays(7));
        testTask.setWorkspace(testWorkspace);
        testTask.setCreatedBy(testUser);
        testTask.setAssignedTo(assignedUser);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        testSubtask = new Subtask();
        testSubtask.setId(1L);
        testSubtask.setTitle("Test Subtask");
        testSubtask.setCompleted(false);
        testSubtask.setTask(testTask);

        createTaskDto = new CreateTaskDto();
        createTaskDto.setTitle("New Task");
        createTaskDto.setDescription("New Description");
        createTaskDto.setPriority("HIGH");
        createTaskDto.setStatus("IN_PROGRESS");
        createTaskDto.setDueDate(LocalDate.now().plusDays(5));
        createTaskDto.setAssignedToId("assigned-user-id");
        
        CreateSubtaskDto subtaskDto = new CreateSubtaskDto();
        subtaskDto.setTitle("New Subtask");
        subtaskDto.setCompleted(false);
        createTaskDto.setSubtasks(List.of(subtaskDto));
    }

    @Test
    void getTasksByWorkspace_ShouldReturnTaskList_WhenTasksExist() {
        // Given
        List<Task> tasks = List.of(testTask);
        List<Subtask> subtasks = List.of(testSubtask);

        when(taskRepository.findByWorkspaceId(anyLong())).thenReturn(tasks);
        when(subtaskRepository.findByTaskId(anyLong())).thenReturn(subtasks);

        // When
        List<TaskDto> result = taskService.getTasksByWorkspace(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        TaskDto taskDto = result.get(0);
        assertEquals(testTask.getTitle(), taskDto.getTitle());
        assertEquals(testTask.getDescription(), taskDto.getDescription());
        assertEquals(1, taskDto.getSubtasks().size());
        
        verify(taskRepository).findByWorkspaceId(1L);
        verify(subtaskRepository).findByTaskId(1L);
    }

    @Test
    void getTasksByWorkspace_ShouldReturnEmptyList_WhenNoTasksExist() {
        // Given
        when(taskRepository.findByWorkspaceId(anyLong())).thenReturn(new ArrayList<>());

        // When
        List<TaskDto> result = taskService.getTasksByWorkspace(1L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository).findByWorkspaceId(1L);
    }

    @Test
    void getTasksByWorkspaceAndStatus_ShouldReturnFilteredTasks_WhenTasksWithStatusExist() {
        // Given
        List<Task> tasks = List.of(testTask);
        List<Subtask> subtasks = List.of(testSubtask);

        when(taskRepository.findByWorkspaceIdAndStatus(anyLong(), any(TaskStatus.class))).thenReturn(tasks);
        when(subtaskRepository.findByTaskId(anyLong())).thenReturn(subtasks);

        // When
        List<TaskDto> result = taskService.getTasksByWorkspaceAndStatus(1L, TaskStatus.TODO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask.getTitle(), result.get(0).getTitle());
        
        verify(taskRepository).findByWorkspaceIdAndStatus(1L, TaskStatus.TODO);
        verify(subtaskRepository).findByTaskId(1L);
    }

    @Test
    void getTasksByUser_ShouldReturnUserTasks_WhenUserExists() {
        // Given
        List<Task> tasks = List.of(testTask);
        List<Subtask> subtasks = List.of(testSubtask);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(assignedUser));
        when(taskRepository.findByAssignedToId(anyString())).thenReturn(tasks);
        when(subtaskRepository.findByTaskId(anyLong())).thenReturn(subtasks);

        // When
        List<TaskDto> result = taskService.getTasksByUser("assigned@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask.getTitle(), result.get(0).getTitle());
        
        verify(userRepository).findByEmail("assigned@example.com");
        verify(taskRepository).findByAssignedToId("assigned-user-id");
        verify(subtaskRepository).findByTaskId(1L);
    }

    @Test
    void getTasksByUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.getTasksByUser("nonexistent@example.com")
        );
        
        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
        verifyNoInteractions(taskRepository);
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenTaskExists() {
        // Given
        List<Subtask> subtasks = List.of(testSubtask);

        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(testTask));
        when(subtaskRepository.findByTaskId(anyLong())).thenReturn(subtasks);

        // When
        TaskDto result = taskService.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTitle(), result.getTitle());
        assertEquals(1, result.getSubtasks().size());
        
        verify(taskRepository).findById(1L);
        verify(subtaskRepository).findByTaskId(1L);
    }

    @Test
    void getTaskById_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.getTaskById(999L)
        );
        
        assertEquals("Task not found with id: 999", exception.getMessage());
        verify(taskRepository).findById(999L);
        verifyNoInteractions(subtaskRepository);
    }

    @Test
    void createTask_ShouldReturnTaskDto_WhenValidInput() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("assigned-user-id")).thenReturn(Optional.of(assignedUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(subtaskRepository.save(any(Subtask.class))).thenReturn(testSubtask);
        when(subtaskRepository.findByTaskId(anyLong())).thenReturn(List.of(testSubtask));

        // When
        TaskDto result = taskService.createTask(1L, "test@example.com", createTaskDto);

        // Then
        assertNotNull(result);
        assertEquals(testTask.getTitle(), result.getTitle());
        
        verify(workspaceRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).findById("assigned-user-id");
        verify(taskRepository).save(any(Task.class));
        verify(subtaskRepository).save(any(Subtask.class));
    }

    @Test
    void createTask_ShouldCreateTaskWithoutAssignedUser_WhenAssignedToIdIsNull() {
        // Given
        createTaskDto.setAssignedToId(null);
        
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(subtaskRepository.save(any(Subtask.class))).thenReturn(testSubtask);
        when(subtaskRepository.findByTaskId(anyLong())).thenReturn(List.of(testSubtask));

        // When
        TaskDto result = taskService.createTask(1L, "test@example.com", createTaskDto);

        // Then
        assertNotNull(result);
        verify(workspaceRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).findById(anyString());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowException_WhenWorkspaceNotFound() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.createTask(999L, "test@example.com", createTaskDto)
        );
        
        assertEquals("Workspace not found with id: 999", exception.getMessage());
        verify(workspaceRepository).findById(999L);
        verifyNoInteractions(taskRepository);
    }

    @Test
    void createTask_ShouldThrowException_WhenCreatedByUserNotFound() {
        // Given
        when(workspaceRepository.findById(anyLong())).thenReturn(Optional.of(testWorkspace));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.createTask(1L, "nonexistent@example.com", createTaskDto)
        );
        
        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        verify(workspaceRepository).findById(1L);
        verify(userRepository).findByEmail("nonexistent@example.com");
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask_WhenTaskExists() {
        // Given
        CreateTaskDto updateDto = new CreateTaskDto();
        updateDto.setTitle("Updated Task");
        updateDto.setDescription("Updated Description");
        updateDto.setPriority("LOW");
        updateDto.setStatus("DONE");
        updateDto.setAssignedToId("assigned@example.com");
        updateDto.setSubtasks(new ArrayList<>());

        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(testTask));
        when(userRepository.findByEmail("assigned@example.com")).thenReturn(Optional.of(assignedUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(subtaskRepository.findByTaskId(anyLong())).thenReturn(new ArrayList<>());

        // When
        TaskDto result = taskService.updateTask(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(taskRepository).findById(1L);
        verify(userRepository).findByEmail("assigned@example.com");
        verify(taskRepository).save(testTask);
        verify(subtaskRepository).deleteByTaskId(1L);
    }

    @Test
    void updateTask_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.updateTask(999L, createTaskDto)
        );
        
        assertEquals("Task not found with id: 999", exception.getMessage());
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTask_ShouldDeleteTask_WhenTaskExists() {
        // Given
        when(taskRepository.existsById(anyLong())).thenReturn(true);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> taskService.deleteTask(999L)
        );
        
        assertEquals("Task not found with id: 999", exception.getMessage());
        verify(taskRepository).existsById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }
} 