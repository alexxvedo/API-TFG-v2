package com.example.api_v2.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API.
 * Intercepta las excepciones y las convierte en respuestas de error estructuradas.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de tipo ApiError.
     */
    @ExceptionHandler(ApiError.class)
    public ResponseEntity<ErrorResponse> handleApiError(ApiError ex, WebRequest request) {
        log.error("API Error: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromApiError(ex, path);
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    /**
     * Maneja excepciones de recursos no encontrados.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.error("Recurso no encontrado: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de recursos duplicados.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        
        log.error("Recurso duplicado: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.RESOURCE_ALREADY_EXISTS, ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones de acceso no autorizado.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        
        log.error("Acceso no autorizado: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.FORBIDDEN, ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Maneja excepciones de permisos insuficientes.
     */
    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermissionsException(
            InsufficientPermissionsException ex, WebRequest request) {
        
        log.error("Permisos insuficientes: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.FORBIDDEN, ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Maneja excepciones de validación de argumentos de método.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("Error de validación: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.VALIDATION_ERROR, path, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja excepciones de operaciones inválidas.
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(
            InvalidOperationException ex, WebRequest request) {
        
        log.error("Operación inválida: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.INVALID_REQUEST, ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja excepciones de estado de entidad inválido.
     */
    @ExceptionHandler(EntityStateException.class)
    public ResponseEntity<ErrorResponse> handleEntityStateException(
            EntityStateException ex, WebRequest request) {
        
        log.error("Estado de entidad inválido: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.INVALID_REQUEST, ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja excepciones de integridad de datos.
     */
    @ExceptionHandler(DataIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(
            DataIntegrityException ex, WebRequest request) {
        
        log.error("Error de integridad de datos: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.VALIDATION_ERROR, ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja excepciones genéricas de negocio.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        log.error("Error de negocio: {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ex.getErrorCode(), ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }
    
    /**
     * Maneja excepciones de parámetros faltantes.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        log.error("Parámetro faltante: {}", ex.getMessage());
        
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.MISSING_PARAMETERS, 
                "El parámetro '" + ex.getParameterName() + "' es requerido", 
                path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja excepciones de tipo de argumento incorrecto.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.error("Tipo de argumento incorrecto: {}", ex.getMessage());
        
        String path = request.getDescription(false).replace("uri=", "");
        String message = "El parámetro '" + ex.getName() + "' debe ser de tipo " + 
                ex.getRequiredType().getSimpleName();
        
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.INVALID_REQUEST, message, path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja excepciones de mensajes HTTP no legibles.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        log.error("Mensaje HTTP no legible: {}", ex.getMessage());
        
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.INVALID_REQUEST, 
                "El cuerpo de la solicitud no es válido o está mal formateado", 
                path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja excepciones de enlace.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, WebRequest request) {
        
        log.error("Error de enlace: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.VALIDATION_ERROR, path, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maneja todas las demás excepciones no capturadas específicamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.INTERNAL_SERVER_ERROR, path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
