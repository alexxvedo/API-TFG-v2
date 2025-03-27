package com.example.api_v2.controller;

import com.example.api_v2.dto.UserDto;
import com.example.api_v2.dto.UserStatsDto;
import com.example.api_v2.dto.WorkspaceDto;
import com.example.api_v2.model.PermissionType;
import com.example.api_v2.service.UserStatsService;
import com.example.api_v2.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserStatsController {

    private final UserStatsService userStatsService;

    @GetMapping("/stats/{email}")
    public ResponseEntity<UserStatsDto> getUserStats(@PathVariable("email") String email) {
        UserStatsDto userStats = userStatsService.getUserStats(email);
        log.info("Returning user stats for user {}", email);
        return ResponseEntity.ok(userStats);
    }
}