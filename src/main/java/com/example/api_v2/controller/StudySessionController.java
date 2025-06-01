package com.example.api_v2.controller;

import com.example.api_v2.dto.StudySessionDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.service.StudySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study-sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping
    public ResponseEntity<StudySessionDto> createStudySession(@RequestBody StudySessionDto studySessionDto) {
        log.info("Creando sesión de estudio: {}", studySessionDto);
        
        // Validar el DTO de la sesión de estudio
        if (studySessionDto == null) {
            log.error("Datos de sesión de estudio no proporcionados");
            ErrorUtils.throwValidationError("Los datos de la sesión de estudio son obligatorios");
        }
        
        // Validar campos obligatorios en el DTO
        
        if (studySessionDto.getUser() == null) {
            log.error("Usuario no proporcionado en la sesión de estudio");
            ErrorUtils.throwValidationError("El usuario es obligatorio para la sesión de estudio");
        } else if (studySessionDto.getUser().getEmail() == null || studySessionDto.getUser().getEmail().trim().isEmpty()) {
            log.error("Email de usuario no proporcionado en la sesión de estudio");
            ErrorUtils.throwValidationError("El email del usuario es obligatorio para la sesión de estudio");
        }
        
        if (studySessionDto.getCollectionId() == null || studySessionDto.getCollectionId() <= 0) {
            log.error("ID de colección inválido en la sesión de estudio: {}", studySessionDto.getCollectionId());
            ErrorUtils.throwValidationError("El ID de la colección debe ser un número positivo");
        }
        
        StudySessionDto createdSession = studySessionService.createStudySession(studySessionDto);
        log.info("Sesión de estudio creada con ID: {}", createdSession.getId());
        return ResponseEntity.ok(createdSession);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySessionDto> getStudySession(@PathVariable("id") Long id) {
        log.info("Obteniendo sesión de estudio con ID: {}", id);
        
        // Validar el ID de la sesión de estudio
        if (id == null || id <= 0) {
            log.error("ID de sesión de estudio inválido: {}", id);
            ErrorUtils.throwValidationError("El ID de la sesión de estudio debe ser un número positivo");
        }
        
        StudySessionDto session = studySessionService.getStudySession(id);
        log.info("Sesión de estudio obtenida: {}", session);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<StudySessionDto> completeStudySession(@PathVariable("id") Long id) {
        log.info("Completando sesión de estudio con ID: {}", id);
        
        // Validar el ID de la sesión de estudio
        if (id == null || id <= 0) {
            log.error("ID de sesión de estudio inválido: {}", id);
            ErrorUtils.throwValidationError("El ID de la sesión de estudio debe ser un número positivo");
        }
        
        StudySessionDto completedSession = studySessionService.completeStudySession(id);
        log.info("Sesión de estudio completada: {}", completedSession);
        return ResponseEntity.ok(completedSession);
    }
}