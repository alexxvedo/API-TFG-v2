package com.example.api_v2.service;

import com.example.api_v2.dto.StudySessionDto;
import com.example.api_v2.model.*;
import com.example.api_v2.repository.CollectionRepository;
import com.example.api_v2.repository.FlashcardRepository;
import com.example.api_v2.repository.StudySessionRepository;
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

    public StudySessionDto createStudySession(StudySessionDto studySessionDto) {
        Collection collection = collectionRepository.findById(studySessionDto.getCollectionId())
                .orElseThrow(() -> new EntityNotFoundException("Collection not found"));

        StudySession studySession = new StudySession();
        studySession.setCollection(collection);
        studySession.setStartTime(LocalDateTime.now());
        studySession.setCompleted(false);

        studySession = studySessionRepository.save(studySession);
        return convertToDto(studySession);
    }

    public StudySessionDto getStudySession(Long id) {
        StudySession studySession = studySessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Study session not found"));

        return convertToDto(studySession);
    }

    public StudySessionDto addActivity(Long sessionId, Long flashcardId, StudySessionDto.FlashcardActivityDto activityDto) {
        StudySession studySession = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Study session not found"));

        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));

        FlashcardActivity activity = new FlashcardActivity();
        activity.setFlashcard(flashcard);
        activity.setStudySession(studySession);
        activity.setCorrect(activityDto.isCorrect());
        activity.setNewStatus(activityDto.getNewStatus());
        activity.setUserAnswer(activityDto.getUserAnswer());

        studySession.addActivity(activity);

        // Actualizar el estado de la flashcard
        flashcard.setStatus(activity.getNewStatus());
        flashcardRepository.save(flashcard);

        studySession = studySessionRepository.save(studySession);
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

        List<StudySessionDto.FlashcardActivityDto> activities = studySession.getActivities().stream()
                .map(activity -> {
                    StudySessionDto.FlashcardActivityDto activityDto = new StudySessionDto.FlashcardActivityDto();
                    activityDto.setId(activity.getId());
                    activityDto.setFlashcardId(activity.getFlashcard().getId());
                    activityDto.setCorrect(activity.isCorrect());
                    activityDto.setTimestamp(activity.getTimestamp());
                    activityDto.setNewStatus(activity.getNewStatus());
                    activityDto.setUserAnswer(activity.getUserAnswer());
                    return activityDto;
                })
                .collect(Collectors.toList());

        dto.setActivities(activities);
        return dto;
    }
}

