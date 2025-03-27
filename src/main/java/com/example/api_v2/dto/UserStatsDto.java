package com.example.api_v2.dto;

import lombok.Data;

@Data
public class UserStatsDto {
    private Long id;
    private String createdAt;
    private String updatedAt;
    private UserDto user;
    private Integer createdFlashcards;
    private Integer studySeconds;
    private Integer studiedFlashcards;
    private Integer expLevel;
    private Integer currentLevelExp;
}