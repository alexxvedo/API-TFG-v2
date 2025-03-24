package com.example.api_v2.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WorkspaceActivityDto {
  private Long id;
  private WorkspaceDto workspace;
  private UserDto user;
  private String action;
  private LocalDateTime timestamp;
}
