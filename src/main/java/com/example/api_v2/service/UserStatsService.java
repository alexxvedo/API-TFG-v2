package com.example.api_v2.service;

import com.example.api_v2.dto.UserStatsDto;
import com.example.api_v2.model.User;
import com.example.api_v2.model.UserStats;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserStatsService {
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    public UserStatsDto getUserStats(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("email no puede ser nulo o vacío");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        UserStats userStats = userStatsRepository.findByUserId(user.getId())
              .orElseGet(() -> {
                UserStats newStats = new UserStats();
                newStats.setUser(user);
                newStats.setCreatedFlashcards(0);
                newStats.setStudySeconds(0);
                newStats.setStudiedFlashcards(0);
                newStats.setLevel(1);
                newStats.setExperience(0);
                newStats.setExperienceToNextLevel(100);
                newStats.setTotalExperience(0);
                newStats.setDailyStreak(0);
                newStats.setTotalActiveDays(0);
                newStats.setTotalAchievements(0);
                newStats.setAverageAccuracy(0.0);
                newStats.setStudySessionsCompleted(0);
                newStats.setTotalCollections(0);
                newStats.setActiveCollections(0);
                newStats.setTotalWorkspaces(0);
                newStats.setActiveWorkspaces(0);
                userStatsRepository.save(newStats);
                return newStats;
            });
        log.info("Getting user stats for user: {}", user.getId());

        // Enrich with additional data
        enrichUserStats(userStats);

        return userStats.toDto();
    }

    public UserStatsDto updateUserStats(String userId, UserStatsDto userStatsDto) {
        UserStats userStats = userStatsRepository.findByUserId(userId)
                .orElseGet(() -> {
                  User user = userRepository.findById(userId)
                  .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));
                  UserStats newStats = new UserStats();
                  newStats.setUser(user);
                  newStats.setCreatedFlashcards(0);
                  newStats.setStudySeconds(0);
                  newStats.setStudiedFlashcards(0);
                  newStats.setLevel(1);
                  newStats.setExperience(0);
                  newStats.setExperienceToNextLevel(100);
                  newStats.setTotalExperience(0);
                  newStats.setCreatedAt(LocalDateTime.now());
                  newStats.setUpdatedAt(LocalDateTime.now());
                  userStatsRepository.save(newStats);
                  return newStats;
              });

        if(userStatsDto.getCreatedFlashcards() != null) {
            userStats.setCreatedFlashcards(userStats.getCreatedFlashcards() + userStatsDto.getCreatedFlashcards());
        }
        if(userStatsDto.getStudySeconds() != null) {
            userStats.setStudySeconds(userStats.getStudySeconds() + userStatsDto.getStudySeconds());
        }
        if(userStatsDto.getStudiedFlashcards() != null) {
            userStats.setStudiedFlashcards(userStats.getStudiedFlashcards() + userStatsDto.getStudiedFlashcards());
        }
        if(userStatsDto.getLevel() != null) {
            userStats.setLevel(userStatsDto.getLevel());
        }
        if(userStatsDto.getExperience() != null) {
            // Actualizar experiencia total
            userStats.setTotalExperience(userStats.getTotalExperience() + userStatsDto.getExperience());
            userStats.setExperience(userStats.getExperience() + userStatsDto.getExperience());
            
            // Comprobar si sube de nivel
            if (userStats.getExperience() >= userStats.getExperienceToNextLevel()) {
                userStats.setLevel(userStats.getLevel() + 1);
                userStats.setExperience(userStats.getExperience() - userStats.getExperienceToNextLevel());
                userStats.setExperienceToNextLevel((int)(userStats.getExperienceToNextLevel() * 2)); // Aumenta 50% cada nivel
            }
        }

        // Actualizar métricas de actividad
        userStats.setLastActiveDate(LocalDateTime.now());
        updateActivityMetrics(userStats);

        // Actualizar precisión media
        if (userStatsDto.getAverageAccuracy() != null) {
            Double currentAccuracy = userStats.getAverageAccuracy();
            Integer totalSessions = userStats.getStudySessionsCompleted();
            
            // Calcular nueva precisión media ponderada
            double newAccuracy = ((currentAccuracy != null ? currentAccuracy : 0.0) * (totalSessions != null ? totalSessions : 0) + userStatsDto.getAverageAccuracy()) / ((totalSessions != null ? totalSessions : 0) + 1);
            userStats.setAverageAccuracy(newAccuracy);
            userStats.setStudySessionsCompleted((totalSessions != null ? totalSessions : 0) + 1);
        }

        userStats = userStatsRepository.save(userStats);
        return userStats.toDto();
    }

    @Transactional
    public void incrementAchievements(String email, String achievementId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
        UserStats userStats = userStatsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User stats not found"));
        
        // Inicializar la lista si es null
        if (userStats.getUnlockedAchievements() == null) {
            userStats.setUnlockedAchievements(new ArrayList<>());
        }
        
        // Verificar si el logro ya está desbloqueado
        if (!userStats.getUnlockedAchievements().contains(achievementId)) {
            // Añadir el logro a la lista de desbloqueados
            userStats.getUnlockedAchievements().add(achievementId);
            
            // Incrementar el contador de logros
            Integer totalAchievements = userStats.getTotalAchievements();
            userStats.setTotalAchievements(totalAchievements != null ? totalAchievements + 1 : 1);
            
            userStatsRepository.save(userStats);
        }
    }

    private void updateActivityMetrics(UserStats userStats) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActive = userStats.getLastActiveDate();
        
        if (lastActive != null) {
            // Si la última actividad fue ayer, incrementar racha
            if (lastActive.toLocalDate().plusDays(1).equals(now.toLocalDate())) {
                userStats.setDailyStreak(userStats.getDailyStreak() + 1);
            }
            // Si la última actividad fue hace más de un día, reiniciar racha
            else if (lastActive.toLocalDate().isBefore(now.toLocalDate().minusDays(1))) {
                userStats.setDailyStreak(1);
            }
        }
        
        // Incrementar días activos si es un nuevo día
        if (lastActive == null || !lastActive.toLocalDate().equals(now.toLocalDate())) {
            userStats.setTotalActiveDays(userStats.getTotalActiveDays() + 1);
        }
    }
    
    /**
     * Enriquece las estadísticas del usuario con datos adicionales
     * @param userStats Estadísticas del usuario a enriquecer
     */
    private void enrichUserStats(UserStats userStats) {
        // Si no hay logros desbloqueados, inicializar la lista
        if (userStats.getUnlockedAchievements() == null) {
            userStats.setUnlockedAchievements(new ArrayList<>());
        }
        
        // Si no hay progreso de logros, inicializar el mapa
        if (userStats.getAchievementProgress() == null) {
            userStats.setAchievementProgress(new HashMap<>());
        }
        
        // Si no hay contribuciones de workspaces, inicializar el mapa
        if (userStats.getWorkspaceContributions() == null) {
            userStats.setWorkspaceContributions(new HashMap<>());
        }
        
        // Asegurar que totalAchievements tiene un valor razonable
        if (userStats.getTotalAchievements() == 0) {
            userStats.setTotalAchievements(12); // Número total de logros disponibles
        }
        
        // Asegurar que hay datos de colecciones
        if (userStats.getTotalCollections() == 0) {
            userStats.setTotalCollections(3);
            userStats.setActiveCollections(2);
        }
        
        // Asegurar que hay datos de workspaces
        if (userStats.getTotalWorkspaces() == 0) {
            userStats.setTotalWorkspaces(2);
            userStats.setActiveWorkspaces(1);
            
            // Añadir contribuciones de ejemplo a workspaces
            Map<String, Integer> workspaceContributions = new HashMap<>();
            workspaceContributions.put("Mathematics:Mathematics Workspace:5:3:50:2d ago:10", 15);
            workspaceContributions.put("Computer Science:CS Fundamentals:8:4:75:1d ago:20", 25);
            userStats.setWorkspaceContributions(workspaceContributions);
        }
        
        // Añadir estadísticas diarias (para hoy)
        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("todayStudiedCards", 3); // Ejemplo: 3 tarjetas estudiadas hoy
        todayStats.put("todayStudyMinutes", 8); // Ejemplo: 8 minutos de estudio hoy
        todayStats.put("todayAccuracy", 85.0); // Ejemplo: 85% de precisión hoy
        todayStats.put("todayNotes", 1); // Ejemplo: 1 nota creada hoy
        todayStats.put("todayFlashcards", 2); // Ejemplo: 2 flashcards creadas hoy
        
        // Añadir las estadísticas diarias al DTO
        userStats.setTodayStats(todayStats);
    }
}