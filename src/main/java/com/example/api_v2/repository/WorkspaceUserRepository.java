package com.example.api_v2.repository;

import com.example.api_v2.model.WorkspaceUser;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {

    WorkspaceUser findByUserId(String userId);


    List<WorkspaceUser> findAllByUserId(String userId);

    WorkspaceUser findByUserIdAndWorkspaceId(String userEmail, Long workspaceId);
}
