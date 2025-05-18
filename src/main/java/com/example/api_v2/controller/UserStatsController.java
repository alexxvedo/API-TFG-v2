package com.example.api_v2.controller;

import com.example.api_v2.dto.UserStatsDto;
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
        log.info("Getting user stats for user {}", email);
        UserStatsDto userStats = userStatsService.getUserStats(email);
        log.info("Returning user stats for user {}", email);
        return ResponseEntity.ok(userStats);
    }

    @PostMapping("/{email}/achievement-completed")
    public ResponseEntity<Void> achievementCompleted(
        @PathVariable("email") String email,
        @RequestParam("achievementId") String achievementId
    ) {
        userStatsService.incrementAchievements(email, achievementId);
        return ResponseEntity.ok().build();
    }
}