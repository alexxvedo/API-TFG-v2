package com.example.api_v2.controller;

import com.example.api_v2.dto.UserActivityDto;
import com.example.api_v2.dto.UserMonthlyStatsDto;
import com.example.api_v2.dto.UserWeeklyStatsDto;
import com.example.api_v2.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/activity")
@RequiredArgsConstructor
public class UserActivityController {
    private final UserActivityService userActivityService;

    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<UserActivityDto>> getRecentActivity(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userActivityService.getRecentActivity(userId));
    }

    @GetMapping("/weekly/{userId}")
    public ResponseEntity<UserWeeklyStatsDto> getWeeklyStats(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userActivityService.getWeeklyStats(userId));
    }

    @GetMapping("/monthly/{userId}")
    public ResponseEntity<UserMonthlyStatsDto> getMonthlyStats(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userActivityService.getMonthlyStats(userId));
    }
}
