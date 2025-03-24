package com.example.api_v2.controller;

import com.example.api_v2.dto.UserFlashcardProgressDto;
import com.example.api_v2.service.UserFlashcardProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flashcards/progress")
@RequiredArgsConstructor
public class UserFlashcardProgressController {

    private final UserFlashcardProgressService userFlashcardProgressService;

    @PutMapping()
    public ResponseEntity<UserFlashcardProgressDto> updateProgress(
            @RequestBody UserFlashcardProgressDto progressDto) {
        
        UserFlashcardProgressDto updatedProgress = userFlashcardProgressService.updateProgress(progressDto);
        return ResponseEntity.ok(updatedProgress);
    }
}
