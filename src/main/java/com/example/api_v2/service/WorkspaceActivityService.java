package com.example.api_v2.service;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.dto.WorkspaceActivityDto;
import com.example.api_v2.model.Collection;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.model.User;
import com.example.api_v2.model.WorkspaceActivity;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceActivityRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceActivityService {

    private final WorkspaceActivityRepository workspaceActivityRepository;

    public List<WorkspaceActivityDto> getActivitiesByWorkspace(Long workspaceId) {
        return workspaceActivityRepository.findAllByWorkspaceIdOrderByTimestampDesc(workspaceId).stream()
                .map(WorkspaceActivity::toDto)
                .collect(Collectors.toList());
    }

}