package com.example.api_v2.service;

import com.example.api_v2.dto.UserStatsDto;
import com.example.api_v2.model.User;
import com.example.api_v2.model.UserStats;
import com.example.api_v2.model.Workspace;
import com.example.api_v2.model.WorkspaceActivity;
import com.example.api_v2.model.WorkspaceUser;
import com.example.api_v2.repository.UserRepository;
import com.example.api_v2.repository.UserStatsRepository;
import com.example.api_v2.repository.WorkspaceActivityRepository;
import com.example.api_v2.repository.WorkspaceRepository;
import com.example.api_v2.repository.WorkspaceUserRepository;
import com.example.api_v2.repository.UserFlashcardProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                newStats.setExpLevel(0);
                newStats.setCurrentLevelExp(0);
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
                  newStats.setExpLevel(0);
                  newStats.setCurrentLevelExp(0);
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
        if(userStatsDto.getExpLevel() != null) {
            userStats.setExpLevel(userStats.getExpLevel() + userStatsDto.getExpLevel());
        }
        if(userStatsDto.getCurrentLevelExp() != null) {
            userStats.setCurrentLevelExp(userStats.getCurrentLevelExp() + userStatsDto.getCurrentLevelExp());
        }

        userStats = userStatsRepository.save(userStats);
        return userStats.toDto();
    }

    


}