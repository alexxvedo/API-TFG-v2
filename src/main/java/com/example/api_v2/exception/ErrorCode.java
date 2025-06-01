package com.example.api_v2.exception;

import org.springframework.http.HttpStatus;

/**
 * Enum que define los códigos de error de la aplicación.
 * Cada código tiene un mensaje predeterminado y un estado HTTP asociado.
 */
public enum ErrorCode {
    // Errores de autenticación y autorización
    UNAUTHORIZED("No estás autorizado para realizar esta acción", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("No tienes permisos suficientes para acceder a este recurso", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("Credenciales inválidas", HttpStatus.UNAUTHORIZED),
    
    // Errores de recursos
    RESOURCE_NOT_FOUND("Recurso no encontrado", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS("El recurso ya existe", HttpStatus.CONFLICT),
    
    // Errores de validación
    VALIDATION_ERROR("Error de validación", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("Solicitud inválida", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETERS("Faltan parámetros requeridos", HttpStatus.BAD_REQUEST),
    
    // Errores de operaciones
    OPERATION_FAILED("La operación ha fallado", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR("Error en servicio externo", HttpStatus.SERVICE_UNAVAILABLE),
    
    // Errores generales
    INTERNAL_SERVER_ERROR("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_IMPLEMENTED("Funcionalidad no implementada", HttpStatus.NOT_IMPLEMENTED);
    
    private final String defaultMessage;
    private final HttpStatus httpStatus;
    
    ErrorCode(String defaultMessage, HttpStatus httpStatus) {
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
