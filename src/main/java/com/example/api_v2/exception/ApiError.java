package com.example.api_v2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Clase que representa un error de API.
 * Contiene un código de error, un mensaje y opcionalmente detalles adicionales.
 */
@Getter
public class ApiError extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String message;
    private final Object details;
    
    /**
     * Constructor con código de error y mensaje personalizado.
     *
     * @param errorCode Código de error
     * @param message   Mensaje personalizado
     */
    public ApiError(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.details = null;
    }
    
    /**
     * Constructor con código de error, mensaje personalizado y detalles.
     *
     * @param errorCode Código de error
     * @param message   Mensaje personalizado
     * @param details   Detalles adicionales del error
     */
    public ApiError(ErrorCode errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }
    
    /**
     * Constructor con solo código de error (usa el mensaje predeterminado).
     *
     * @param errorCode Código de error
     */
    public ApiError(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getDefaultMessage();
        this.details = null;
    }
    
    /**
     * Constructor con código de error y detalles (usa el mensaje predeterminado).
     *
     * @param errorCode Código de error
     * @param details   Detalles adicionales del error
     */
    public ApiError(ErrorCode errorCode, Object details) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getDefaultMessage();
        this.details = details;
    }
    
    /**
     * Obtiene el estado HTTP asociado con este error.
     *
     * @return Estado HTTP
     */
    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
