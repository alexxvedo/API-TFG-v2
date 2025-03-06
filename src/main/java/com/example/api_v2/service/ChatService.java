package com.example.api_v2.service;

import com.example.api_v2.dto.ChatDto;
import com.example.api_v2.dto.MessageDto;
import com.example.api_v2.dto.UserDto;
import com.example.api_v2.model.Chat;
import com.example.api_v2.model.Message;
import com.example.api_v2.model.User;
import com.example.api_v2.repository.ChatRepository;
import com.example.api_v2.repository.MessageRepository;
import com.example.api_v2.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatDto getChat(Long workspaceId) {
        return chatRepository.findByWorkspaceId(workspaceId)
                .map(this::mapToChatDto)
                .orElseGet(() -> createChat(workspaceId));
    }

    @Transactional
    public MessageDto addMessage(Long workspaceId, String content, String senderEmail) {
        Chat chat = chatRepository.findByWorkspaceId(workspaceId)
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .workspaceId(workspaceId)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return chatRepository.save(newChat);
                });

        User user = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Message message = Message.builder()
                .content(content)
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userImage(user.getImage())
                .timestamp(LocalDateTime.now())
                .chat(chat)
                .build();

        message = messageRepository.save(message);
        
        return mapToMessageDto(message);
    }

    private ChatDto createChat(Long workspaceId) {
        Chat chat = Chat.builder()
                .workspaceId(workspaceId)
                .createdAt(LocalDateTime.now())
                .build();
        
        Chat savedChat = chatRepository.save(chat);
        return mapToChatDto(savedChat);
    }

    private ChatDto mapToChatDto(Chat chat) {
        return ChatDto.builder()
                .id(chat.getId())
                .workspaceId(chat.getWorkspaceId())
                .createdAt(chat.getCreatedAt())
                .messages(chat.getMessages().stream()
                        .map(this::mapToMessageDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private MessageDto mapToMessageDto(Message message) {
        UserDto sender = new UserDto();
        sender.setId(message.getUserId());
        sender.setName(message.getUserName());
        sender.setEmail(message.getUserEmail());
        sender.setImage(message.getUserImage());

        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .sender(sender)
                .timestamp(message.getTimestamp())
                .build();
    }
}
