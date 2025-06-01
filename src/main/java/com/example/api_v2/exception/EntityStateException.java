package com.example.api_v2.exception;

/**
 * Excepción para estados inválidos de entidades.
 * Se utiliza cuando una entidad está en un estado que no permite realizar cierta operación.
 */
public class EntityStateException extends BusinessException {
    
    public EntityStateException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
    
    public EntityStateException(String entityName, String currentState, String requiredAction) {
        super(ErrorCode.INVALID_REQUEST, 
              String.format("No se puede %s el/la %s porque está en estado '%s'", 
                           requiredAction, entityName, currentState));
    }
}
