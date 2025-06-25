package com.example.api_v2.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkspaceDto {
    private Long id;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private String user;
    private List<WorkspaceUserDto> users;
    private List<CollectionDto> collections;
}