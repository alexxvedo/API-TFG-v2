package com.example.api_v2.service;

import com.example.api_v2.dto.UserFlashcardProgressDto;
import com.example.api_v2.model.UserFlashcardProgress;
import com.example.api_v2.repository.UserFlashcardProgressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFlashcardProgressService {

    private final UserFlashcardProgressRepository userFlashcardProgressRepository;

    public UserFlashcardProgressDto updateProgress(UserFlashcardProgressDto progressDto) {
      
        UserFlashcardProgress progress = userFlashcardProgressRepository.findByFlashcardIdAndUserId(progressDto.getFlashcardId(), progressDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("UserFlashcardProgress not found"));

        // Aplicar el algoritmo SM-2 para determinar el nuevo intervalo de repetición
        int newRepetitionLevel = progress.getRepetitionLevel();
        double newEaseFactor = progress.getEaseFactor();
        int interval = 1;

        System.out.println("ProgressDto: " + progressDto);

        if ("CORRECT".equals(progressDto.getReviewResult())) {
            if (newRepetitionLevel == 0) {
                interval = 1;
            } else if (newRepetitionLevel == 1) {
                interval = 3;
            } else {
                interval = (int) Math.round(newRepetitionLevel * newEaseFactor);
            }
            newRepetitionLevel++;
            newEaseFactor = Math.max(1.3, newEaseFactor + 0.1);
        } else if ("PARTIAL".equals(progressDto.getReviewResult())) {
            interval = 1;
            newEaseFactor = Math.max(1.3, newEaseFactor - 0.1);
        } else if ("WRONG".equals(progressDto.getReviewResult())) {
            newRepetitionLevel = 0;
            interval = 1;
            newEaseFactor = Math.max(1.3, newEaseFactor - 0.2);
        }

        // Calcular la nueva fecha de revisión
        LocalDateTime nextReviewDate = LocalDateTime.now().plusDays(interval);

        // Actualizar valores en la entidad
        progress.setRepetitionLevel(newRepetitionLevel);
        progress.setEaseFactor(newEaseFactor);
        progress.setNextReviewDate(nextReviewDate);
        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setReviewCount(progress.getReviewCount() + 1);

        // Guardar cambios
        userFlashcardProgressRepository.save(progress);

        // Convertir a DTO y devolver
        return progress.toDto();
    }
}
