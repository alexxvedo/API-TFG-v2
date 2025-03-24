package com.example.api_v2.repository;

import com.example.api_v2.model.WorkspaceActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceActivityRepository extends JpaRepository<WorkspaceActivity, Long> {
    List<WorkspaceActivity> findAllByWorkspaceIdOrderByTimestampDesc(Long workspaceId);
}