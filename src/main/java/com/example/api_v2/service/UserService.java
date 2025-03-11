package com.example.api_v2.service;

import com.example.api_v2.dto.UserDto;
import com.example.api_v2.model.PermissionType;
import com.example.api_v2.model.User;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.WorkspaceUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;

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
            // Creamos el workspace por defecto
            Workspace workspace = new Workspace();
            workspace.setName("My Workspace");
            workspace.setDescription("Default Workspace");

            // Guardamos primero el Workspace antes de referenciarlo
            workspace = workspaceRepository.save(workspace);

            // Creamos el WorkspaceUser del usuario para el workspace por defecto
            workspaceUser = new WorkspaceUser();
            workspaceUser.setWorkspace(workspace);
            workspaceUser.setUser(user);
            workspaceUser.setPermissionType(PermissionType.OWNER);

            // Guardamos el WorkspaceUser después de que el Workspace ya existe en la BD
            workspaceUser = workspaceUserRepository.save(workspaceUser);
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