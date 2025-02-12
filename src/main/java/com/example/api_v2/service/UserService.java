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

    public UserDto getUserByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId).map(User::toDto)
                .orElse(null);
    }

    public User createUser(String clerkId, String email, String firstName, String lastName, String profileImageUrl) {

        if (clerkId == null || clerkId.isEmpty()) {
            throw new IllegalArgumentException("clerkId no puede ser nulo o vacío");
        }
        User user = userRepository.findByClerkId(clerkId).orElse(null);

        // Si no existe el usuario lo crea, en caso de que exista simplemente lo devuelve
        if (user == null) {
            user = new User();
            user.setClerkId(clerkId);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setProfileImageUrl(profileImageUrl);
            user.setUpdatedAt(LocalDateTime.now());

            // Guardamos primero el usuario
            user = userRepository.save(user);

            // Creamos el workspace por defecto
            Workspace workspace = new Workspace();
            workspace.setName("My Workspace");
            workspace.setDescription("Default Workspace");
            workspace.setCreatedAt(LocalDateTime.now());
            workspace.setUpdatedAt(LocalDateTime.now());

            // 🔹 Guardamos primero el Workspace antes de referenciarlo
            workspace = workspaceRepository.save(workspace);

            // Creamos el WorkspaceUser del usuario para el workspace por defecto
            WorkspaceUser workspaceUser = new WorkspaceUser();
            workspaceUser.setWorkspace(workspace);
            workspaceUser.setUser(user);
            workspaceUser.setPermissionType(PermissionType.OWNER);
            workspaceUser.setCreatedAt(LocalDateTime.now());
            workspaceUser.setUpdatedAt(LocalDateTime.now());

            // 🔹 Guardamos el WorkspaceUser después de que el Workspace ya existe en la BD
            workspaceUser = workspaceUserRepository.save(workspaceUser);

            // Asociamos el usuario al workspace y guardamos de nuevo si es necesario
            workspace.getUsers().add(workspaceUser);
            workspaceRepository.save(workspace);
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