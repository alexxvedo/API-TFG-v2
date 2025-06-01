package com.example.api_v2.repository;

import com.example.api_v2.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByWorkspaceId(Long workspaceId);
    Optional<Collection> findByWorkspaceIdAndName(Long workspaceId, String name);
}

