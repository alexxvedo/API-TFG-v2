package com.example.api_v2.util;

import com.example.api_v2.exception.ErrorCode;
import com.example.api_v2.exception.ErrorUtils;

/**
 * Clase de ejemplo que muestra cómo utilizar el sistema centralizado de manejo de errores.
 * Esta clase no está destinada a ser utilizada en producción, sino como referencia.
 */
public class ErrorExampleService {

    /**
     * Ejemplo de cómo lanzar un error cuando un recurso no se encuentra.
     * 
     * @param id ID del recurso
     */
    public void ejemploRecursoNoEncontrado(String id) {
        // Verificar si el recurso existe (simulado)
        boolean existe = false;
        
        if (!existe) {
            // Usar la utilidad para lanzar el error
            ErrorUtils.throwResourceNotFound("Usuario", "id", id);
        }
    }
    
    /**
     * Ejemplo de cómo lanzar un error cuando un usuario no tiene permisos.
     * 
     * @param userId ID del usuario
     * @param recursoId ID del recurso
     */
    public void ejemploUsuarioSinPermisos(String userId, String recursoId) {
        // Verificar si el usuario tiene permisos (simulado)
        boolean tienePermisos = false;
        
        if (!tienePermisos) {
            // Usar la utilidad para lanzar el error
            ErrorUtils.throwUnauthorizedForAction(userId, "editar", "documento " + recursoId);
        }
    }
    
    /**
     * Ejemplo de cómo lanzar un error de validación.
     * 
     * @param email Email a validar
     */
    public void ejemploValidacionEmail(String email) {
        // Validar el formato del email (simulado)
        boolean formatoValido = email != null && email.contains("@");
        
        if (!formatoValido) {
            // Usar la utilidad para lanzar el error
            ErrorUtils.throwValidationError("El formato del email no es válido");
        }
    }
    
    /**
     * Ejemplo de cómo lanzar un error con detalles adicionales.
     * 
     * @param datos Datos a validar
     */
    public void ejemploErrorConDetalles(Object datos) {
        // Simular validación con múltiples errores
        if (datos == null) {
            // Crear un mapa con los detalles de los errores
            java.util.Map<String, String> errores = new java.util.HashMap<>();
            errores.put("nombre", "El nombre es obligatorio");
            errores.put("email", "El email es obligatorio");
            errores.put("edad", "La edad debe ser mayor a 18");
            
            // Usar la utilidad para lanzar el error con detalles
            ErrorUtils.throwValidationError("Varios campos son inválidos", errores);
        }
    }
    
    /**
     * Ejemplo de cómo lanzar un error personalizado.
     * 
     * @param operacion Nombre de la operación
     */
    public void ejemploErrorPersonalizado(String operacion) {
        // Simular un error en una operación específica
        ErrorUtils.throwApiError(
                ErrorCode.OPERATION_FAILED,
                "La operación '" + operacion + "' ha fallado",
                "Detalles técnicos del error"
        );
    }
}
