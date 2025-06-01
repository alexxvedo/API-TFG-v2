package com.example.api_v2.controller;

import com.example.api_v2.dto.ChatDto;
import com.example.api_v2.dto.ChatMessageRequest;
import com.example.api_v2.dto.MessageDto;
import com.example.api_v2.exception.ErrorUtils;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ChatDto> getChat(@PathVariable("workspaceId") Long workspaceId) {
        log.info("Obteniendo chat para el workspace: {}", workspaceId);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        ChatDto chat = chatService.getChat(workspaceId);
        if (chat == null) {
            log.error("No se encontró el chat para el workspace: {}", workspaceId);
            throw new ResourceNotFoundException("No se encontró el chat para el workspace: " + workspaceId);
        }
        
        log.info("Chat obtenido para el workspace {}: {} mensajes", workspaceId, 
                chat.getMessages() != null ? chat.getMessages().size() : 0);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/workspace/{workspaceId}/message")
    public ResponseEntity<MessageDto> addMessage(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody ChatMessageRequest request) {
        log.info("Añadiendo mensaje al chat del workspace {}: {}", workspaceId, request);
        
        // Validar los parámetros de entrada
        if (workspaceId == null || workspaceId <= 0) {
            log.error("ID de workspace inválido: {}", workspaceId);
            ErrorUtils.throwValidationError("El ID del workspace debe ser un número positivo");
        }
        
        // Validar el DTO del mensaje
        if (request == null) {
            log.error("Datos de mensaje no proporcionados");
            ErrorUtils.throwValidationError("Los datos del mensaje son obligatorios");
        }
        
        // Ya que hemos verificado que request no es nulo, podemos acceder a sus propiedades
        if (request != null) {
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                log.error("Contenido del mensaje no proporcionado");
                ErrorUtils.throwValidationError("El contenido del mensaje es obligatorio");
            }
            
            if (request.getSenderEmail() == null || request.getSenderEmail().trim().isEmpty()) {
                log.error("Email del remitente no proporcionado");
                ErrorUtils.throwValidationError("El email del remitente es obligatorio");
            }
            
            // Ahora sabemos que request, content y senderEmail no son nulos
            MessageDto message = chatService.addMessage(workspaceId, request.getContent(), request.getSenderEmail());
            log.info("Mensaje añadido al chat del workspace {}: {}", workspaceId, message.getId());
            return ResponseEntity.ok(message);
        }
        
        // Este código nunca se ejecutará debido a la validación anterior, pero es necesario para que compile
        log.error("Error inesperado: request es nulo después de la validación");
        ErrorUtils.throwValidationError("Error inesperado al procesar el mensaje");
        return null; // Este return nunca se ejecutará, pero es necesario para que compile
    }
}
