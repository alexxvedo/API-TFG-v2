package com.example.api_v2.controller;

import com.example.api_v2.dto.UserStatsDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/stats")
@RequiredArgsConstructor
public class UserStatsController {
    private final UserStatsService userStatsService;

    @GetMapping("/{email}")
    public ResponseEntity<UserStatsDto> getUserStats(@PathVariable("email") String email) {
        log.info("Obteniendo estadísticas para el usuario {}", email);
        
        // Validar los parámetros de entrada
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        UserStatsDto userStats = userStatsService.getUserStats(email);
        log.info("Estadísticas obtenidas para el usuario {}", email);
        return ResponseEntity.ok(userStats);
    }

    @PostMapping("/{email}/achievement-completed")
    public ResponseEntity<Void> achievementCompleted(
        @PathVariable("email") String email,
        @RequestParam("achievementId") String achievementId
    ) {
        log.info("Registrando logro completado {} para el usuario {}", achievementId, email);
        
        // Validar los parámetros de entrada
        if (email == null || email.trim().isEmpty()) {
            log.error("Email de usuario inválido: {}", email);
            ErrorUtils.throwValidationError("El email del usuario es obligatorio");
        }
        
        if (achievementId == null || achievementId.trim().isEmpty()) {
            log.error("ID de logro inválido: {}", achievementId);
            ErrorUtils.throwValidationError("El ID del logro es obligatorio");
        }
        
        userStatsService.incrementAchievements(email, achievementId);
        log.info("Logro {} registrado correctamente para el usuario {}", achievementId, email);
        return ResponseEntity.ok().build();
    }
}