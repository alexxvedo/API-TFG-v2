package com.example.api_v2.controller;

import com.example.api_v2.dto.FlashcardDto;
import com.example.api_v2.dto.FlashcardGenerationDto;
import com.example.api_v2.dto.FlashcardReviewDto;
import com.example.api_v2.dto.FlashcardStatsDto;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.service.AIService;
import com.example.api_v2.service.FlashcardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/collections")
@CrossOrigin(origins = "*")
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final AIService aiService;

    public FlashcardController(FlashcardService flashcardService, AIService aiService) {
        this.flashcardService = flashcardService;
        this.aiService = aiService;
    }

    @GetMapping("/{collectionId}/flashcards")
    public ResponseEntity<List<FlashcardDto>> getFlashcardsByCollection(@PathVariable("collectionId") Long collectionId) {
        return ResponseEntity.ok(flashcardService.getFlashcardsByCollection(collectionId));
    }

    @GetMapping("/{collectionId}/flashcards/review")
    public ResponseEntity<List<Flashcard>> getFlashcardsForReview(@PathVariable("collectionId") Long collectionId) {
        List<Flashcard> flashcards = flashcardService.getFlashcardsForReview(collectionId);
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/{collectionId}/stats")
    public ResponseEntity<FlashcardStatsDto> getFlashcardStats(@PathVariable("collectionId") Long collectionId) {
        return ResponseEntity.ok(flashcardService.getFlashcardStats(collectionId));
    }

    @PostMapping("/{collectionId}/flashcards/user/{email}")
    public ResponseEntity<FlashcardDto> createFlashcard(
            @PathVariable("collectionId") Long collectionId,
            @RequestBody FlashcardDto flashcardDto,
            @PathVariable("email") String email) {
        return ResponseEntity.ok(flashcardService.createFlashcard(collectionId, flashcardDto, email));
    }

    @PostMapping("/{collectionId}/generate")
    public ResponseEntity<Map<String, Object>> generateFlashcards(
            @PathVariable("collectionId") Long collectionId,
            @RequestBody FlashcardGenerationDto request) {

        log.info("Generating flashcards, {}", request );

        Map<String, Object> result = aiService.generateFlashcards(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{collectionId}/flashcards/{flashcardId}")
    public ResponseEntity<FlashcardDto> updateFlashcard(
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("flashcardId") Long flashcardId,
            @RequestBody FlashcardDto flashcardDto) {
        return ResponseEntity.ok(flashcardService.updateFlashcard(flashcardId, flashcardDto));
    }

    @DeleteMapping("/{collectionId}/flashcards/{flashcardId}")
    public ResponseEntity<Void> deleteFlashcard(@PathVariable("flashcardId") Long flashcardId) {
        flashcardService.deleteFlashcard(flashcardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{collectionId}/flashcards/{flashcardId}/review")
    public ResponseEntity<Flashcard> submitReview(
            @PathVariable("collectionId") Long collectionId,
            @PathVariable("flashcardId") Long flashcardId,
            @RequestBody FlashcardReviewDto reviewDto) {
        Flashcard flashcard = flashcardService.processReview(flashcardId, reviewDto);
        return ResponseEntity.ok(flashcard);
    }
}

