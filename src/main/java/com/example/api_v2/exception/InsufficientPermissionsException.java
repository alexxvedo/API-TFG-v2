package com.example.api_v2.exception;

/**
 * Excepción para permisos insuficientes.
 * Se utiliza cuando un usuario intenta realizar una acción para la que no tiene permisos.
 */
public class InsufficientPermissionsException extends BusinessException {
    
    public InsufficientPermissionsException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
    
    public InsufficientPermissionsException(String userId, String action, String resource) {
        super(ErrorCode.FORBIDDEN, 
              String.format("El usuario %s no tiene permisos para %s el recurso %s", userId, action, resource));
    }
}
