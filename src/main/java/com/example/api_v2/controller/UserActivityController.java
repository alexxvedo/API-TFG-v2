package com.example.api_v2.controller;

import com.example.api_v2.dto.UserActivityDto;
import com.example.api_v2.dto.UserMonthlyStatsDto;
import com.example.api_v2.dto.UserWeeklyStatsDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/activity")
@RequiredArgsConstructor
@Slf4j
public class UserActivityController {
    private final UserActivityService userActivityService;

    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<UserActivityDto>> getRecentActivity(@PathVariable("userId") String userId) {
        log.info("Obteniendo actividad reciente para el usuario: {}", userId);
        
        // Validar el ID del usuario
        if (userId == null || userId.trim().isEmpty()) {
            log.error("ID de usuario inválido: {}", userId);
            ErrorUtils.throwValidationError("El ID del usuario es obligatorio");
        }
        
        List<UserActivityDto> activities = userActivityService.getRecentActivity(userId);
        log.info("Se encontraron {} actividades recientes para el usuario {}", activities.size(), userId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/weekly/{userId}")
    public ResponseEntity<UserWeeklyStatsDto> getWeeklyStats(@PathVariable("userId") String userId) {
        log.info("Obteniendo estadísticas semanales para el usuario: {}", userId);
        
        // Validar el ID del usuario
        if (userId == null || userId.trim().isEmpty()) {
            log.error("ID de usuario inválido: {}", userId);
            ErrorUtils.throwValidationError("El ID del usuario es obligatorio");
        }
        
        UserWeeklyStatsDto stats = userActivityService.getWeeklyStats(userId);
        log.info("Estadísticas semanales obtenidas para el usuario {}", userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/monthly/{userId}")
    public ResponseEntity<UserMonthlyStatsDto> getMonthlyStats(@PathVariable("userId") String userId) {
        log.info("Obteniendo estadísticas mensuales para el usuario: {}", userId);
        
        // Validar el ID del usuario
        if (userId == null || userId.trim().isEmpty()) {
            log.error("ID de usuario inválido: {}", userId);
            ErrorUtils.throwValidationError("El ID del usuario es obligatorio");
        }
        
        UserMonthlyStatsDto stats = userActivityService.getMonthlyStats(userId);
        log.info("Estadísticas mensuales obtenidas para el usuario {}", userId);
        return ResponseEntity.ok(stats);
    }
}
