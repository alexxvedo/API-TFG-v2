package com.example.api_v2.service;

import com.example.api_v2.model.PermissionType;
import com.example.api_v2.model.User;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.WorkspaceUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceAuthorizationService {

    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;

    /**
     * Verifica si un usuario tiene acceso a un workspace
     * @param email Email del usuario
     * @param workspaceId ID del workspace
     * @return true si tiene acceso, false en caso contrario
     */
    public boolean hasWorkspaceAccess(String email, Long workspaceId) {
        log.info("Verificando acceso al workspace {} para el usuario {}", workspaceId, email);
        
        if (email == null || workspaceId == null) {
            log.warn("Email o workspaceId nulos, denegando acceso");
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Usuario no encontrado: {}", email);
            return false;
        }
        
        User user = userOpt.get();
        WorkspaceUser workspaceUser = workspaceUserRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId);
        
        boolean hasAccess = workspaceUser != null;
        
        if (hasAccess) {
            log.info("Usuario {} tiene acceso al workspace {} con rol {}", 
                    email, workspaceId, workspaceUser != null ? workspaceUser.getPermissionType() : "NINGUNO");
        } else {
            log.warn("Usuario {} no tiene acceso al workspace {}", email, workspaceId);
        }
        
        return hasAccess;
    }
    
    /**
     * Verifica si un usuario tiene un rol específico o superior en un workspace
     * @param email Email del usuario
     * @param workspaceId ID del workspace
     * @param requiredPermission Permiso requerido
     * @return true si tiene el permiso requerido o superior, false en caso contrario
     */
    public boolean hasWorkspacePermission(String email, Long workspaceId, PermissionType requiredPermission) {
        log.debug("Verificando permiso {} en workspace {} para usuario {}", requiredPermission, workspaceId, email);
        
        if (email == null || workspaceId == null || requiredPermission == null) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Usuario no encontrado: {}", email);
            return false;
        }
        
        User user = userOpt.get();
        WorkspaceUser workspaceUser = workspaceUserRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId);
        
        if (workspaceUser == null) {
            log.warn("Usuario {} no tiene acceso al workspace {}", email, workspaceId);
            return false;
        }
        
        // Orden de permisos: OWNER > EDITOR > VIEWER
        List<PermissionType> permissionHierarchy = Arrays.asList(
            PermissionType.VIEWER, 
            PermissionType.EDITOR, 
            PermissionType.OWNER
        );
        
        int userPermissionLevel = permissionHierarchy.indexOf(workspaceUser.getPermissionType());
        int requiredPermissionLevel = permissionHierarchy.indexOf(requiredPermission);
        
        return userPermissionLevel >= requiredPermissionLevel;
    }
    
    /**
     * Verifica si un usuario es propietario de un workspace
     * @param email Email del usuario
     * @param workspaceId ID del workspace
     * @return true si es propietario, false en caso contrario
     */
    public boolean isWorkspaceOwner(String email, Long workspaceId) {
        return hasWorkspacePermission(email, workspaceId, PermissionType.OWNER);
    }
    
    /**
     * Verifica si un usuario es editor o propietario de un workspace
     * @param email Email del usuario
     * @param workspaceId ID del workspace
     * @return true si es editor o propietario, false en caso contrario
     */
    public boolean canEditWorkspace(String email, Long workspaceId) {
        return hasWorkspacePermission(email, workspaceId, PermissionType.EDITOR);
    }
}
