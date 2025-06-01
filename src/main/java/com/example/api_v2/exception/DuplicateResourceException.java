package com.example.api_v2.exception;

/**
 * Excepción para recursos duplicados.
 * Se utiliza cuando se intenta crear un recurso que ya existe.
 */
public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }
    
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, 
              String.format("Ya existe un/a %s con %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
