package com.example.api_v2.exception;

/**
 * Excepción para problemas de integridad de datos.
 * Se utiliza cuando hay violaciones de integridad referencial o restricciones de datos.
 */
public class DataIntegrityException extends BusinessException {
    
    public DataIntegrityException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }
    
    public DataIntegrityException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
    }
    
    public DataIntegrityException(String entityName, String relationName, String reason) {
        super(ErrorCode.VALIDATION_ERROR, 
              String.format("No se puede completar la operación en %s debido a la relación con %s: %s", 
                           entityName, relationName, reason));
    }
}
