package com.example.api_v2.exception;

/**
 * Clase de utilidad para lanzar errores de forma centralizada.
 * Proporciona métodos estáticos para generar diferentes tipos de errores
 * con mensajes personalizados.
 */
public class ErrorUtils {
    
    //region Errores de recursos
    
    /**
     * Lanza un error de recurso no encontrado.
     *
     * @param message Mensaje de error
     * @throws ResourceNotFoundException Excepción lanzada
     */
    public static void throwResourceNotFound(String message) {
        throw new ResourceNotFoundException(message);
    }
    
    /**
     * Lanza un error de recurso no encontrado con un formato específico.
     *
     * @param resourceName Nombre del recurso
     * @param fieldName    Nombre del campo
     * @param fieldValue   Valor del campo
     * @throws ResourceNotFoundException Excepción lanzada
     */
    public static void throwResourceNotFound(String resourceName, String fieldName, Object fieldValue) {
        throw new ResourceNotFoundException(
                String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue));
    }
    
    /**
     * Lanza un error de recurso duplicado.
     *
     * @param message Mensaje de error
     * @throws DuplicateResourceException Excepción lanzada
     */
    public static void throwDuplicateResource(String message) {
        throw new DuplicateResourceException(message);
    }
    
    /**
     * Lanza un error de recurso duplicado con un formato específico.
     *
     * @param resourceName Nombre del recurso
     * @param fieldName    Nombre del campo
     * @param fieldValue   Valor del campo
     * @throws DuplicateResourceException Excepción lanzada
     */
    public static void throwDuplicateResource(String resourceName, String fieldName, Object fieldValue) {
        throw new DuplicateResourceException(resourceName, fieldName, fieldValue);
    }
    
    //endregion
    
    //region Errores de autorización
    
    /**
     * Lanza un error de acceso no autorizado.
     *
     * @param message Mensaje de error
     * @throws UnauthorizedException Excepción lanzada
     */
    public static void throwUnauthorized(String message) {
        throw new UnauthorizedException(message);
    }
    
    /**
     * Lanza un error de acceso no autorizado para una acción específica.
     *
     * @param userId    ID del usuario
     * @param action    Acción que se intentó realizar
     * @param resource  Recurso sobre el que se intentó actuar
     * @throws UnauthorizedException Excepción lanzada
     */
    public static void throwUnauthorizedForAction(String userId, String action, String resource) {
        throw new UnauthorizedException(
                String.format("Usuario %s no tiene permiso para %s el recurso %s", userId, action, resource));
    }
    
    /**
     * Lanza un error de permisos insuficientes.
     *
     * @param message Mensaje de error
     * @throws InsufficientPermissionsException Excepción lanzada
     */
    public static void throwInsufficientPermissions(String message) {
        throw new InsufficientPermissionsException(message);
    }
    
    /**
     * Lanza un error de permisos insuficientes para una acción específica.
     *
     * @param userId    ID del usuario
     * @param action    Acción que se intentó realizar
     * @param resource  Recurso sobre el que se intentó actuar
     * @throws InsufficientPermissionsException Excepción lanzada
     */
    public static void throwInsufficientPermissions(String userId, String action, String resource) {
        throw new InsufficientPermissionsException(userId, action, resource);
    }
    
    //endregion
    
    //region Errores de operaciones
    
    /**
     * Lanza un error de operación inválida.
     *
     * @param message Mensaje de error
     * @throws InvalidOperationException Excepción lanzada
     */
    public static void throwInvalidOperation(String message) {
        throw new InvalidOperationException(message);
    }
    
    /**
     * Lanza un error de estado de entidad inválido.
     *
     * @param message Mensaje de error
     * @throws EntityStateException Excepción lanzada
     */
    public static void throwEntityStateError(String message) {
        throw new EntityStateException(message);
    }
    
    /**
     * Lanza un error de estado de entidad inválido con formato específico.
     *
     * @param entityName    Nombre de la entidad
     * @param currentState  Estado actual de la entidad
     * @param requiredAction Acción que se intentó realizar
     * @throws EntityStateException Excepción lanzada
     */
    public static void throwEntityStateError(String entityName, String currentState, String requiredAction) {
        throw new EntityStateException(entityName, currentState, requiredAction);
    }
    
    /**
     * Lanza un error de integridad de datos.
     *
     * @param message Mensaje de error
     * @throws DataIntegrityException Excepción lanzada
     */
    public static void throwDataIntegrityError(String message) {
        throw new DataIntegrityException(message);
    }
    
    /**
     * Lanza un error de integridad de datos con formato específico.
     *
     * @param entityName   Nombre de la entidad
     * @param relationName Nombre de la relación
     * @param reason       Razón del error
     * @throws DataIntegrityException Excepción lanzada
     */
    public static void throwDataIntegrityError(String entityName, String relationName, String reason) {
        throw new DataIntegrityException(entityName, relationName, reason);
    }
    
    //endregion
    
    //region Errores genéricos de API
    
    /**
     * Lanza un error genérico de API con un código de error específico.
     *
     * @param errorCode Código de error
     * @throws ApiError Excepción lanzada
     */
    public static void throwApiError(ErrorCode errorCode) {
        throw new ApiError(errorCode);
    }
    
    /**
     * Lanza un error genérico de API con un código de error y mensaje personalizado.
     *
     * @param errorCode Código de error
     * @param message   Mensaje personalizado
     * @throws ApiError Excepción lanzada
     */
    public static void throwApiError(ErrorCode errorCode, String message) {
        throw new ApiError(errorCode, message);
    }
    
    /**
     * Lanza un error genérico de API con un código de error, mensaje personalizado y detalles.
     *
     * @param errorCode Código de error
     * @param message   Mensaje personalizado
     * @param details   Detalles adicionales del error
     * @throws ApiError Excepción lanzada
     */
    public static void throwApiError(ErrorCode errorCode, String message, Object details) {
        throw new ApiError(errorCode, message, details);
    }
    
    //endregion
    
    //region Errores de validación
    
    /**
     * Lanza un error de validación con un mensaje específico.
     *
     * @param message Mensaje de error
     * @throws ApiError Excepción lanzada
     */
    public static void throwValidationError(String message) {
        throw new ApiError(ErrorCode.VALIDATION_ERROR, message);
    }
    
    /**
     * Lanza un error de validación con detalles específicos.
     *
     * @param message Mensaje de error
     * @param details Detalles de la validación fallida
     * @throws ApiError Excepción lanzada
     */
    public static void throwValidationError(String message, Object details) {
        throw new ApiError(ErrorCode.VALIDATION_ERROR, message, details);
    }
    
    /**
     * Lanza un error de negocio genérico.
     *
     * @param message Mensaje de error
     * @throws BusinessException Excepción lanzada
     */
    public static void throwBusinessError(String message) {
        throw new BusinessException(message);
    }
    
    /**
     * Lanza un error de negocio con un código específico.
     *
     * @param errorCode Código de error
     * @param message   Mensaje de error
     * @throws BusinessException Excepción lanzada
     */
    public static void throwBusinessError(ErrorCode errorCode, String message) {
        throw new BusinessException(errorCode, message);
    }
    
    //endregion
    
    //region Otros errores comunes
    
    /**
     * Lanza un error de operación fallida.
     *
     * @param message Mensaje de error
     * @throws ApiError Excepción lanzada
     */
    public static void throwOperationFailed(String message) {
        throw new ApiError(ErrorCode.OPERATION_FAILED, message);
    }
    
    /**
     * Lanza un error de permisos insuficientes (usando ApiError).
     *
     * @param message Mensaje de error
     * @throws ApiError Excepción lanzada
     */
    public static void throwForbidden(String message) {
        throw new ApiError(ErrorCode.FORBIDDEN, message);
    }
    
    /**
     * Lanza un error de solicitud inválida.
     *
     * @param message Mensaje de error
     * @throws ApiError Excepción lanzada
     */
    public static void throwInvalidRequest(String message) {
        throw new ApiError(ErrorCode.INVALID_REQUEST, message);
    }
    
    //endregion
}
