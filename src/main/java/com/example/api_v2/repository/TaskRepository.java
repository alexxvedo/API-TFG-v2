package com.example.api_v2.repository;

import com.example.api_v2.model.Task;
import com.example.api_v2.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByWorkspaceId(Long workspaceId);
    List<Task> findByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);
    List<Task> findByAssignedToId(String userId);
    List<Task> findByWorkspaceIdAndAssignedToId(Long workspaceId, String userId);
}
