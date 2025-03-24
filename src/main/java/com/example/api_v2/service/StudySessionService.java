package com.example.api_v2.service;

import com.example.api_v2.dto.StudySessionDto;
import com.example.api_v2.dto.UserFlashcardProgressDto;
import com.example.api_v2.model.*;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.FlashcardRepository;
import com.example.api_v2.repository.StudySessionRepository;
import com.example.api_v2.repository.UserFlashcardProgressRepository;
import com.example.api_v2.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final CollectionRepository collectionRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserFlashcardProgressRepository userFlashcardProgressRepository;
    private final UserRepository userRepository;

    public StudySessionDto createStudySession(StudySessionDto studySessionDto) {
        Collection collection = collectionRepository.findById(studySessionDto.getCollectionId())
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));
    
        StudySession studySession = new StudySession();
        studySession.setCollection(collection);
        studySession.setStartTime(LocalDateTime.now());
        studySession.setCompleted(false);
    
        System.out.println("📌 Buscando flashcards para colección ID: " + studySessionDto.getUser().getEmail());
    
        User user = userRepository.findByEmail(studySessionDto.getUser().getEmail()).orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Flashcard> flashcardsStudySession = flashcardRepository.findFlashcardsToReviewToday(collection.getId(), user.getId());
    
        System.out.println("📊 Flashcards encontradas: " + flashcardsStudySession.size());
    
        for (Flashcard f : flashcardsStudySession) {
            System.out.println("🃏 Flashcard ID: " + f.getId() + " - Pregunta: " + f.getQuestion() + " - nextReviewDate: " + f.getNextReviewDate());
        }
    
        studySession.setFlashcard(flashcardsStudySession);
        studySession.setTotalCards(flashcardsStudySession.size());
        studySession.setUser(user);
    
        studySession = studySessionRepository.save(studySession);
        return convertToDto(studySession);
    }
    

    public StudySessionDto getStudySession(Long id) {
        StudySession studySession = studySessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Study session not found"));

        return convertToDto(studySession);
    }

    

    public StudySessionDto completeStudySession(Long id) {
        StudySession studySession = studySessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Study session not found"));

        studySession.complete();

        studySession = studySessionRepository.save(studySession);
        return convertToDto(studySession);
    }

    private StudySessionDto convertToDto(StudySession studySession) {
        StudySessionDto dto = new StudySessionDto();
        dto.setId(studySession.getId());
        dto.setCollectionId(studySession.getCollection().getId());
        dto.setStartTime(studySession.getStartTime());
        dto.setEndTime(studySession.getEndTime());
        dto.setCompleted(studySession.isCompleted());
        dto.setTotalCards(studySession.getTotalCards());
        dto.setCorrectAnswers(studySession.getCorrectAnswers());
        dto.setIncorrectAnswers(studySession.getIncorrectAnswers());

        dto.setFlashcards(studySession.getFlashcard().stream().map(Flashcard::toDto).collect(Collectors.toList()));
        return dto;
    }
}
