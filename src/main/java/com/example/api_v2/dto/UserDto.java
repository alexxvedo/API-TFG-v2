package com.example.api_v2.dto;

import java.time.LocalDateTime;

import com.example.api_v2.model.PermissionType;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String image;
    private PermissionType permissionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

