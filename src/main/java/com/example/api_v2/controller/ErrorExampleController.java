package com.example.api_v2.controller;

import com.example.api_v2.exception.ErrorCode;
import com.example.api_v2.exception.ErrorUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de ejemplo que muestra cómo utilizar el sistema centralizado de manejo de errores.
 * Este controlador no está destinado a ser utilizado en producción, sino como referencia.
 */
@RestController
@RequestMapping("/api/ejemplos-error")
public class ErrorExampleController {

    /**
     * Ejemplo de endpoint que lanza un error de recurso no encontrado.
     * 
     * @param id ID del recurso
     * @return Nunca retorna un valor, siempre lanza una excepción
     */
    @GetMapping("/recurso-no-encontrado/{id}")
    public ResponseEntity<Object> ejemploRecursoNoEncontrado(@PathVariable String id) {
        // Simular búsqueda de recurso
        ErrorUtils.throwResourceNotFound("Usuario", "id", id);
        return null; // Nunca se ejecuta
    }
    
    /**
     * Ejemplo de endpoint que lanza un error de acceso no autorizado.
     * 
     * @param userId ID del usuario
     * @param recursoId ID del recurso
     * @return Nunca retorna un valor, siempre lanza una excepción
     */
    @GetMapping("/sin-permisos")
    public ResponseEntity<Object> ejemploSinPermisos(
            @RequestParam String userId, 
            @RequestParam String recursoId) {
        // Simular verificación de permisos
        ErrorUtils.throwUnauthorizedForAction(userId, "editar", "documento " + recursoId);
        return null; // Nunca se ejecuta
    }
    
    /**
     * Ejemplo de endpoint que lanza un error de validación.
     * 
     * @param email Email a validar
     * @return Nunca retorna un valor, siempre lanza una excepción
     */
    @GetMapping("/validacion-email")
    public ResponseEntity<Object> ejemploValidacionEmail(@RequestParam String email) {
        // Validar email
        if (email == null || !email.contains("@")) {
            ErrorUtils.throwValidationError("El formato del email no es válido");
        }
        return ResponseEntity.ok("Email válido");
    }
    
    /**
     * Ejemplo de endpoint que lanza un error con detalles adicionales.
     * 
     * @return Nunca retorna un valor, siempre lanza una excepción
     */
    @GetMapping("/error-con-detalles")
    public ResponseEntity<Object> ejemploErrorConDetalles() {
        // Crear un mapa con los detalles de los errores
        java.util.Map<String, String> errores = new java.util.HashMap<>();
        errores.put("nombre", "El nombre es obligatorio");
        errores.put("email", "El email es obligatorio");
        errores.put("edad", "La edad debe ser mayor a 18");
        
        // Lanzar error con detalles
        ErrorUtils.throwValidationError("Varios campos son inválidos", errores);
        return null; // Nunca se ejecuta
    }
    
    /**
     * Ejemplo de endpoint que lanza un error personalizado.
     * 
     * @param operacion Nombre de la operación
     * @return Nunca retorna un valor, siempre lanza una excepción
     */
    @GetMapping("/error-personalizado")
    public ResponseEntity<Object> ejemploErrorPersonalizado(@RequestParam String operacion) {
        // Lanzar error personalizado
        ErrorUtils.throwApiError(
                ErrorCode.OPERATION_FAILED,
                "La operación '" + operacion + "' ha fallado",
                "Detalles técnicos del error"
        );
        return null; // Nunca se ejecuta
    }
    
    /**
     * Ejemplo de endpoint que muestra cómo manejar errores en un bloque try-catch.
     * 
     * @return Respuesta exitosa o error
     */
    @GetMapping("/manejo-try-catch")
    public ResponseEntity<Object> ejemploManejoTryCatch() {
        try {
            // Simular operación que puede fallar
            boolean operacionExitosa = Math.random() > 0.5;
            
            if (!operacionExitosa) {
                throw new RuntimeException("Error interno simulado");
            }
            
            return ResponseEntity.ok("Operación exitosa");
        } catch (Exception e) {
            // Convertir la excepción en un ApiError
            ErrorUtils.throwApiError(
                    ErrorCode.OPERATION_FAILED, 
                    "La operación ha fallado", 
                    e.getMessage()
            );
            return null; // Nunca se ejecuta
        }
    }
}
