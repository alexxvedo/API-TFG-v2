package com.example.api_v2.security;

import com.example.api_v2.service.WorkspaceAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class WorkspaceSecurityAspect {

    private final WorkspaceAuthorizationService workspaceAuthorizationService;

    @Before("@annotation(com.example.api_v2.security.WorkspaceAccess)")
    public void checkWorkspaceAccess(JoinPoint joinPoint) {
        log.info("Verificando acceso al workspace");

        String userEmail = getUserEmail();
        if (userEmail == null) {
            log.warn("No se encontró usuario autenticado");
            throw new SecurityException("Usuario no autenticado");
        }

        try {
            Long workspaceId = getWorkspaceId(joinPoint, WorkspaceAccess.class);
            log.info("Verificando acceso del usuario {} al workspace {}", userEmail, workspaceId);

            boolean hasAccess = workspaceAuthorizationService.hasWorkspaceAccess(userEmail, workspaceId);
            if (!hasAccess) {
                log.warn("Acceso denegado para el usuario {} al workspace {}", userEmail, workspaceId);
                throw new SecurityException(
                        "No tienes permisos para acceder a este workspace. Por favor, verifica tus permisos o contacta con el administrador.");
            }

            log.info("Acceso concedido para el usuario {} al workspace {}", userEmail, workspaceId);
        } catch (IllegalArgumentException e) {
            log.error("Error al obtener el ID del workspace: {}", e.getMessage());
            throw new SecurityException("Error al verificar permisos: " + e.getMessage());
        }
    }

    @Before("@annotation(com.example.api_v2.security.WorkspaceEditAccess)")
    public void checkWorkspaceEditAccess(JoinPoint joinPoint) {
        log.info("Verificando acceso de edición al workspace");

        String userEmail = getUserEmail();
        if (userEmail == null) {
            log.warn("No se encontró usuario autenticado");
            throw new SecurityException("Usuario no autenticado");
        }

        try {
            Long workspaceId = getWorkspaceId(joinPoint, WorkspaceEditAccess.class);
            log.info("Verificando acceso de edición del usuario {} al workspace {}", userEmail, workspaceId);

            boolean hasEditAccess = workspaceAuthorizationService.canEditWorkspace(userEmail, workspaceId);
            if (!hasEditAccess) {
                log.warn("Acceso de edición denegado para el usuario {} al workspace {}", userEmail, workspaceId);
                throw new SecurityException("No tienes permisos de edición para este workspace");
            }

            log.info("Acceso de edición concedido para el usuario {} al workspace {}", userEmail, workspaceId);
        } catch (IllegalArgumentException e) {
            log.error("Error al obtener el ID del workspace: {}", e.getMessage());
            throw new SecurityException("Error al verificar permisos de edición: " + e.getMessage());
        }
    }

    @Before("@annotation(com.example.api_v2.security.WorkspaceOwnerAccess)")
    public void checkWorkspaceOwnerAccess(JoinPoint joinPoint) {
        log.info("Verificando acceso de propietario al workspace");

        String userEmail = getUserEmail();
        if (userEmail == null) {
            log.warn("No se encontró usuario autenticado");
            throw new SecurityException("Usuario no autenticado");
        }

        try {
            Long workspaceId = getWorkspaceId(joinPoint, WorkspaceOwnerAccess.class);
            log.info("Verificando acceso de propietario del usuario {} al workspace {}", userEmail, workspaceId);

            // Orden correcto de parámetros según la firma del método en
            // WorkspaceAuthorizationService
            boolean isOwner = workspaceAuthorizationService.isWorkspaceOwner(userEmail, workspaceId);
            if (!isOwner) {
                log.warn("Acceso de propietario denegado para el usuario {} al workspace {}", userEmail, workspaceId);
                throw new SecurityException("Debes ser el propietario del workspace para realizar esta acción");
            }

            log.info("Acceso de propietario concedido para el usuario {} al workspace {}", userEmail, workspaceId);
        } catch (IllegalArgumentException e) {
            log.error("Error al obtener el ID del workspace: {}", e.getMessage());
            throw new SecurityException("Error al verificar permisos de propietario: " + e.getMessage());
        }
    }

    private String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    private Long getWorkspaceId(JoinPoint joinPoint, Class<? extends Annotation> annotationClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String paramName;

        if (annotationClass == WorkspaceAccess.class) {
            paramName = method.getAnnotation(WorkspaceAccess.class).workspaceIdParam();
        } else if (annotationClass == WorkspaceEditAccess.class) {
            paramName = method.getAnnotation(WorkspaceEditAccess.class).workspaceIdParam();
        } else if (annotationClass == WorkspaceOwnerAccess.class) {
            paramName = method.getAnnotation(WorkspaceOwnerAccess.class).workspaceIdParam();
        } else {
            throw new IllegalArgumentException("Anotación no soportada: " + annotationClass.getName());
        }

        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // Verificar si parameterNames es null
        if (parameterNames == null) {
            log.warn("Los nombres de parámetros no están disponibles. Intentando obtener el ID por posición.");
            // Intentar obtener el ID del primer parámetro (común en controladores REST)
            if (args.length > 0 && args[0] != null) {
                if (args[0] instanceof Long) {
                    return (Long) args[0];
                } else if (args[0] instanceof String) {
                    try {
                        return Long.parseLong((String) args[0]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("El primer parámetro no es un ID válido");
                    }
                }
            }
            throw new IllegalArgumentException("No se pudo determinar el ID del workspace");
        }

        // Buscar el parámetro por nombre
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(paramName)) {
                if (args[i] == null) {
                    throw new IllegalArgumentException("El parámetro " + paramName + " es nulo");
                }

                if (args[i] instanceof Long) {
                    return (Long) args[i];
                } else if (args[i] instanceof String) {
                    try {
                        return Long.parseLong((String) args[i]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("El parámetro " + paramName + " no es un ID válido");
                    }
                }
            }
        }

        // Si llegamos aquí, no se encontró el parámetro
        log.error("No se encontró el parámetro {} en el método {}. Parámetros disponibles: {}",
                paramName, method.getName(), String.join(", ", parameterNames));

        throw new IllegalArgumentException(
                "No se encontró el parámetro " + paramName + " en el método " + method.getName());
    }
}
