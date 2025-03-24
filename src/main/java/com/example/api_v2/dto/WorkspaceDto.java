package com.example.api_v2.dto;

import com.example.api_v2.dto.CollectionDto;
import com.example.api_v2.dto.WorkspaceUserDto;
import lombok.Data;

@Data
public class WorkspaceDto {
    private Long id;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private WorkspaceUserDto users;
    private CollectionDto collections;
}