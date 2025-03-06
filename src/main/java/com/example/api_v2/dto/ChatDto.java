package com.example.api_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private Long id;
    private Long workspaceId;
    private LocalDateTime createdAt;
    private List<MessageDto> messages;
}
