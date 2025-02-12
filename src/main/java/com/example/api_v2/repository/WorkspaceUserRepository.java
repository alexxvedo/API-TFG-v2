package com.example.api_v2.repository;

import com.example.api_v2.model.WorkspaceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {
}
