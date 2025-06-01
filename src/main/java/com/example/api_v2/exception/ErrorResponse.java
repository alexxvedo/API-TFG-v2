package com.example.api_v2.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Clase que representa la estructura de respuesta para errores.
 * Se utiliza para mantener un formato consistente en todas las respuestas de error.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private Object details;
    
    /**
     * Método estático para crear una respuesta de error a partir de un ApiError.
     *
     * @param apiError Error de API
     * @param path     Ruta de la solicitud
     * @return Respuesta de error formateada
     */
    public static ErrorResponse fromApiError(ApiError apiError, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(apiError.getHttpStatus().value())
                .error(apiError.getHttpStatus().getReasonPhrase())
                .code(apiError.getErrorCode().name())
                .message(apiError.getMessage())
                .path(path)
                .details(apiError.getDetails())
                .build();
    }
    
    /**
     * Método estático para crear una respuesta de error a partir de un ErrorCode.
     *
     * @param errorCode Código de error
     * @param path      Ruta de la solicitud
     * @return Respuesta de error formateada
     */
    public static ErrorResponse fromErrorCode(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.name())
                .message(errorCode.getDefaultMessage())
                .path(path)
                .build();
    }
    
    /**
     * Método estático para crear una respuesta de error a partir de un ErrorCode con mensaje personalizado.
     *
     * @param errorCode Código de error
     * @param message   Mensaje personalizado
     * @param path      Ruta de la solicitud
     * @return Respuesta de error formateada
     */
    public static ErrorResponse fromErrorCode(ErrorCode errorCode, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.name())
                .message(message)
                .path(path)
                .build();
    }
    
    /**
     * Método estático para crear una respuesta de error a partir de un ErrorCode con detalles.
     *
     * @param errorCode Código de error
     * @param path      Ruta de la solicitud
     * @param details   Detalles adicionales del error
     * @return Respuesta de error formateada
     */
    public static ErrorResponse fromErrorCode(ErrorCode errorCode, String path, Object details) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.name())
                .message(errorCode.getDefaultMessage())
                .path(path)
                .details(details)
                .build();
    }
}
