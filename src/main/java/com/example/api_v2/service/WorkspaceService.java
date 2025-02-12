package com.example.api_v2.service;

import com.example.api_v2.dto.WorkspaceDto;
import com.example.api_v2.model.PermissionType;
import com.example.api_v2.model.User;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.WorkspaceUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<WorkspaceDto> getWorkspacesByUserId(String userId) {
        log.info("Getting workspaces for user: {}", userId);

        List<Workspace> workspaces = workspaceRepository.findWorkspacesByUserId(userId);
        log.info("Found {} workspaces", workspaces.size());

        return workspaces.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public WorkspaceDto getWorkspace(Long id) {
        log.info("Getting workspace with id: {}", id);
        return workspaceRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
    }

    @Transactional
    public WorkspaceDto createWorkspace(WorkspaceDto workspaceDto, String userId) {
        log.info("Creating workspace with name: {} for user: {}", workspaceDto.getName(), userId);

        // Crear el workspace
        Workspace workspace = new Workspace();
        workspace.setName(workspaceDto.getName());
        workspace.setDescription(workspaceDto.getDescription());

        // Creamos el WorkspaceUser del usuario que ha creado el workspace
        WorkspaceUser workspaceUser = new WorkspaceUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        workspaceUser.setUser(user);
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setPermissionType(PermissionType.OWNER);
        workspaceUser = workspaceUserRepository.save(workspaceUser);

        // Añadimos el WorkspaceUser al workspace
        workspace.getUsers().add(workspaceUser);

        log.info("Created workspace-user relationship with id: {}", workspaceUser.getId());

        workspace = workspaceRepository.save(workspace);
        log.info("Created workspace with id: {}", workspace.getId());

        return convertToDto(workspace);
    }

    @Transactional
    public WorkspaceDto updateWorkspace(Long id, WorkspaceDto workspaceDto) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        workspace.setName(workspaceDto.getName());

        workspace = workspaceRepository.save(workspace);
        return convertToDto(workspace);
    }

    @Transactional
    public void deleteWorkspace(Long id) {
        workspaceRepository.deleteById(id);
    }

    private WorkspaceDto convertToDto(Workspace workspace) {
        log.info("Converting workspace: {}", workspace);
        WorkspaceDto workspaceDto = new WorkspaceDto();
        workspaceDto.setId(workspace.getId());
        workspaceDto.setName(workspace.getName());
        workspaceDto.setDescription(workspace.getDescription());
        return workspaceDto;
    }
}