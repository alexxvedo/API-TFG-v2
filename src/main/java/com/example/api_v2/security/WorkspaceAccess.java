package com.example.api_v2.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para verificar si un usuario tiene acceso a un workspace.
 * Se debe usar en métodos de controladores que reciben un parámetro workspaceId.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WorkspaceAccess {
    /**
     * Nombre del parámetro que contiene el ID del workspace
     */
    String workspaceIdParam() default "workspaceId";
}
