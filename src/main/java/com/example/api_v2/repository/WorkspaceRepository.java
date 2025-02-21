package com.example.api_v2.repository;

import com.example.api_v2.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    @Query("SELECT w FROM Workspace w JOIN WorkspaceUser wu ON wu.workspace = w WHERE wu.user.id = :userId")
    List<Workspace> findWorkspacesByUserId(@Param("userId") String userId);
}

