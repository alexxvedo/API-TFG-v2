package com.example.api_v2.exception;

/**
 * Excepción para operaciones inválidas según la lógica de negocio.
 * Se utiliza cuando una operación no puede realizarse debido a reglas de negocio.
 */
public class InvalidOperationException extends BusinessException {
    
    public InvalidOperationException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
    
    public InvalidOperationException(String message, Throwable cause) {
        super(ErrorCode.INVALID_REQUEST, message, cause);
    }
}
