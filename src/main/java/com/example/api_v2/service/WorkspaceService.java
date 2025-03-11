package com.example.api_v2.service;

import com.example.api_v2.dto.UserDto;
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
@Transactional
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;

    public List<WorkspaceDto> getWorkspacesByUserEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("email no puede ser nulo o vacío");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        List<WorkspaceUser> workspaceUsers = workspaceUserRepository.findAllByUserId(user.getId());
        log.info("Getting workspaces for user: {}", user.getId());

        // Si el usuario no tiene workspaces, crear uno por defecto
        if (workspaceUsers.isEmpty()) {
            // Creamos el workspace por defecto
            Workspace workspace = new Workspace();
            workspace.setName("My Workspace");
            workspace.setDescription("Default Workspace");

            // Guardamos primero el Workspace antes de referenciarlo
            workspace = workspaceRepository.save(workspace);

            // Creamos el WorkspaceUser del usuario para el workspace por defecto
            WorkspaceUser workspaceUser = new WorkspaceUser();
            workspaceUser.setWorkspace(workspace);
            workspaceUser.setUser(user);
            workspaceUser.setPermissionType(PermissionType.OWNER);

            // Guardamos el WorkspaceUser después de que el Workspace ya existe en la BD
            workspaceUser = workspaceUserRepository.save(workspaceUser);
            workspaceUsers = List.of(workspaceUser);
        }

        List<Workspace> workspaces = workspaceRepository.findWorkspacesByUserId(user.getId());
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

    public WorkspaceDto createWorkspace(WorkspaceDto workspaceDto, String email) {
        log.info("Creating workspace with name: {} for user: {}", workspaceDto.getName(), email);

        // Crear el workspace
        Workspace workspace = new Workspace();
        workspace.setName(workspaceDto.getName());
        workspace.setDescription(workspaceDto.getDescription());
        
        // Guardar primero el workspace para obtener un ID
        workspace = workspaceRepository.save(workspace);
        log.info("Created workspace with id: {}", workspace.getId());

        // Creamos el WorkspaceUser del usuario que ha creado el workspace
        WorkspaceUser workspaceUser = new WorkspaceUser();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        workspaceUser.setUser(user);
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setPermissionType(PermissionType.OWNER);
        
        // Guardar la relación WorkspaceUser
        workspaceUser = workspaceUserRepository.save(workspaceUser);
        log.info("Created workspace-user relationship with id: {}", workspaceUser.getId());

        // Añadimos el WorkspaceUser al workspace
        workspace.getWorkspaceUsers().add(workspaceUser);
        
        // Actualizar el workspace con la nueva relación
        workspace = workspaceRepository.save(workspace);

        return convertToDto(workspace);
    }

    public WorkspaceDto updateWorkspace(Long id, WorkspaceDto workspaceDto) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        workspace.setName(workspaceDto.getName());

        workspace = workspaceRepository.save(workspace);
        return convertToDto(workspace);
    }

    public void deleteWorkspace(Long id) {
        workspaceRepository.deleteById(id);
    }


    public void joinWorkspace(Long id, String email, PermissionType permissionType) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (workspaceUserRepository.findByUserIdAndWorkspaceId(user.getId(), id) != null) {
            throw new RuntimeException("User already joined the workspace");
        }

        WorkspaceUser workspaceUser = new WorkspaceUser();
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setUser(user);
        workspaceUser.setPermissionType(permissionType);
        workspaceUserRepository.save(workspaceUser);

        workspace.getWorkspaceUsers().add(workspaceUser);
        workspace = workspaceRepository.save(workspace);
    }

    public List<UserDto> getWorkspaceUsers(Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        return workspace.getWorkspaceUsers().stream()
                .map(workspaceUser -> {
                    UserDto userDto = new UserDto();
                    userDto.setId(workspaceUser.getUser().getId());
                    userDto.setName(workspaceUser.getUser().getName());
                    userDto.setEmail(workspaceUser.getUser().getEmail());
                    userDto.setImage(workspaceUser.getUser().getImage());
                    userDto.setPermissionType(workspaceUser.getPermissionType());
                    return userDto;
                })
                .collect(Collectors.toList());
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