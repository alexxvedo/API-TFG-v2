package com.example.api_v2.controller;

import com.example.api_v2.dto.ChatDto;
import com.example.api_v2.dto.ChatMessageRequest;
import com.example.api_v2.dto.MessageDto;
import com.example.api_v2.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ChatDto> getChat(@PathVariable("workspaceId") Long workspaceId) {
        ChatDto chat = chatService.getChat(workspaceId);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/workspace/{workspaceId}/message")
    public ResponseEntity<MessageDto> addMessage(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(
                chatService.addMessage(workspaceId, request.getContent(), request.getSenderEmail())
        );
    }
}
