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
                userStatsRepository.save(newStats);
                return newStats;
            });
        log.info("Getting user stats for user: {}", user.getId());

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
    public void incrementAchievements(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
        UserStats userStats = userStatsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User stats not found"));
        
        Integer totalAchievements = userStats.getTotalAchievements();
        userStats.setTotalAchievements(totalAchievements != null ? totalAchievements + 1 : 1);
        userStatsRepository.save(userStats);
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
}