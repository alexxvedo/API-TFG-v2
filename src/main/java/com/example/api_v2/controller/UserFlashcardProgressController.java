package com.example.api_v2.controller;

import com.example.api_v2.dto.UserFlashcardProgressDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.service.UserFlashcardProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flashcards/progress")
@RequiredArgsConstructor
@Slf4j
public class UserFlashcardProgressController {

    private final UserFlashcardProgressService userFlashcardProgressService;

    @PutMapping()
    public ResponseEntity<UserFlashcardProgressDto> updateProgress(
            @RequestBody UserFlashcardProgressDto progressDto) {
        log.info("Actualizando progreso de tarjeta: {}", progressDto);
        
        // Validar el DTO de progreso
        if (progressDto == null) {
            log.error("Datos de progreso no proporcionados");
            ErrorUtils.throwValidationError("Los datos de progreso son obligatorios");
        }
        
        // Ya que hemos verificado que progressDto no es nulo, podemos acceder a sus propiedades
        if (progressDto != null) {
            // Validar campos obligatorios en el DTO
            if (progressDto.getFlashcardId() == null || progressDto.getFlashcardId() <= 0) {
                log.error("ID de tarjeta inválido: {}", progressDto.getFlashcardId());
                ErrorUtils.throwValidationError("El ID de la tarjeta debe ser un número positivo");
            }
            
            if (progressDto.getUserId() == null || progressDto.getUserId().trim().isEmpty()) {
                log.error("ID de usuario inválido: {}", progressDto.getUserId());
                ErrorUtils.throwValidationError("El ID del usuario es obligatorio");
            }
        }
        
        UserFlashcardProgressDto updatedProgress = userFlashcardProgressService.updateProgress(progressDto);
        log.info("Progreso actualizado: {}", updatedProgress);
        return ResponseEntity.ok(updatedProgress);
    }
}
