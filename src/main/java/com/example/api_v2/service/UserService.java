package com.example.api_v2.service;

import com.example.api_v2.dto.UserDto;
import com.example.api_v2.dto.WorkspaceDto;
import com.example.api_v2.model.PermissionType;
import com.example.api_v2.model.User;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceActivity;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceActivityRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.WorkspaceUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceService workspaceService;
    private final UserStatsService userStatsService;

    public UserDto getUser(String id) {
        return userRepository.findById(id).map(User::toDto)
                .orElse(null);
    }



    public User createUser(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id no puede ser nulo o vacío");
        }

        // Intentar encontrar el usuario existente
        User user = userRepository.findById(id).orElse(null);

        // Si el usuario no existe, retornamos null (no deberíamos crear usuarios aquí)
        if (user == null) {
            return null;
        }

        // Buscar si el usuario ya tiene un workspace
        WorkspaceUser workspaceUser = workspaceUserRepository.findByUserId(id);

        // Si el usuario existe pero no tiene workspace, crear uno por defecto
        if (workspaceUser == null) {
            log.error("Creating default workspace for user: {}", id);

            WorkspaceDto workspaceDto = new WorkspaceDto();
            workspaceDto.setName("My Workspace");
            workspaceDto.setDescription("Default Workspace");

            workspaceService.createWorkspace(workspaceDto, user.getEmail());

        }


        return user;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(String clerkId) {
        userRepository.deleteById(clerkId);
    }
}