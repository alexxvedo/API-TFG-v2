package com.example.api_v2.controller;

import com.example.api_v2.dto.StudySessionDto;
import com.example.api_v2.service.StudySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/study-sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping
    public ResponseEntity<StudySessionDto> createStudySession(@RequestBody StudySessionDto studySessionDto) {
        return ResponseEntity.ok(studySessionService.createStudySession(studySessionDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySessionDto> getStudySession(@PathVariable("id") Long id) {
        return ResponseEntity.ok(studySessionService.getStudySession(id));
    }

    @PostMapping("/{sessionId}/activities")
    public ResponseEntity<StudySessionDto> addActivity(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam Long flashcardId,
            @RequestBody StudySessionDto.FlashcardActivityDto activityDto) {
        return ResponseEntity.ok(studySessionService.addActivity(sessionId, flashcardId, activityDto));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<StudySessionDto> completeStudySession(@PathVariable("id") Long id) {
        return ResponseEntity.ok(studySessionService.completeStudySession(id));
    }
}