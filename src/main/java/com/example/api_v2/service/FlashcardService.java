package com.example.api_v2.service;

import com.example.api_v2.dto.*;
import com.example.api_v2.exception.ResourceNotFoundException;
import com.example.api_v2.model.*;
import com.example.api_v2.repository.*;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final CollectionRepository collectionRepository;
    private final FlashcardReviewRepository reviewRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public List<FlashcardDto> getFlashcardsByCollection(Long collectionId) {

        return flashcardRepository.findByCollectionId(collectionId)
                .stream()
                .map(this::convertToDto)  // Convertimos cada flashcard a DTO
                .collect(Collectors.toList());
    }


    @Transactional
    public List<Flashcard> getFlashcardsForReview(Long collectionId) {
        // Obtener todas las flashcards de la colección que necesitan revisión
        List<Flashcard> flashcards = flashcardRepository.findByCollectionId(collectionId);
        LocalDateTime now = LocalDateTime.now();

        return flashcards.stream()
                .filter(flashcard -> {
                    // Si nunca se ha revisado, incluirla
                    if (flashcard.getLastReviewedAt() == null) {
                        return true;
                    }

                    // Si tiene fecha de próxima revisión y es anterior a ahora, incluirla
                    return flashcard.getNextReviewDate() != null &&
                            flashcard.getNextReviewDate().isBefore(now);
                })
                .collect(Collectors.toList());
    }

    public FlashcardStatsDto getFlashcardStats(Long collectionId) {
        List<Flashcard> flashcards = flashcardRepository.findByCollectionId(collectionId);
        FlashcardStatsDto stats = new FlashcardStatsDto();

        if (flashcards.isEmpty()) {
            return stats;
        }

        stats.setTotalFlashcards((long) flashcards.size());

        long reviewedFlashcards = flashcards.stream()
                .filter(f -> f.getLastReviewedAt() != null)
                .count();
        stats.setReviewedFlashcards(reviewedFlashcards);

        stats.setReviewRate(calculateReviewRate(flashcards));
        stats.setSuccessRate(calculateSuccessRate(flashcards));

        return stats;
    }

    @Transactional
    public FlashcardDto createFlashcard(Long collectionId, FlashcardDto flashcardDto, String email) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        User user = userRepository.findByEmail(email).orElse(null);



        Flashcard flashcard = Flashcard.builder()
                .question(flashcardDto.getQuestion())
                .answer(flashcardDto.getAnswer())
                .collection(collection)
                .knowledgeLevel(flashcardDto.getKnowledgeLevel())
                .repetitionLevel(flashcardDto.getRepetitionLevel())
                .nextReviewDate(flashcardDto.getNextReviewDate())
                .lastReviewedAt(flashcardDto.getLastReviewedAt())
                .status(flashcardDto.getStatus())
                .notes(flashcardDto.getNotes())
                .tags(flashcardDto.getTags())
                .createdBy(user) 
                .createdAt(flashcardDto.getCreatedAt())
                .updatedAt(flashcardDto.getUpdatedAt())
                .build();




        flashcard = flashcardRepository.save(flashcard);

        // Forzar la inicialización del objeto para evitar que sea un proxy
        Hibernate.initialize(flashcard.getCreatedBy());

        return convertToDto(flashcard);


    }

    @Transactional
    public FlashcardDto updateFlashcard(Long flashcardId, FlashcardDto flashcardDto) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        updateFlashcardFromDto(flashcard, flashcardDto);
        flashcard.setUpdatedAt(LocalDateTime.now());

        return convertToDto(flashcardRepository.save(flashcard));
    }

    public void deleteFlashcard(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));

        flashcardRepository.delete(flashcard);
    }

    @Transactional
    public Flashcard processReview(Long flashcardId, FlashcardReviewDto reviewDto) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        // Crear y guardar la revisión
        FlashcardReview review = FlashcardReview.builder()
                .flashcard(flashcard)
                .result(reviewDto.getResult())
                .timeSpentMs(reviewDto.getTimeSpentMs())
                .reviewedAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);

        // Actualizar la flashcard según el resultado
        updateFlashcardBasedOnReview(flashcard, reviewDto.getResult());

        return flashcardRepository.save(flashcard);
    }

    private void updateFlashcardBasedOnReview(Flashcard flashcard, String result) {
        LocalDateTime now = LocalDateTime.now();
        flashcard.setLastReviewedAt(now);

        // Asegurarse de que repetitionLevel no sea null
        if (flashcard.getRepetitionLevel() == null) {
            flashcard.setRepetitionLevel(0);
        }

        switch (result) {
            case "WRONG":
                // Reiniciar el nivel de repetición
                flashcard.setRepetitionLevel(0);
                flashcard.setNextReviewDate(now);
                break;
            case "PARTIAL":
                // Mantener el nivel actual pero revisar pronto
                flashcard.setNextReviewDate(now.plusHours(1));
                break;
            case "CORRECT":
                // Aumentar el nivel y calcular siguiente fecha
                int currentLevel = flashcard.getRepetitionLevel();
                flashcard.setRepetitionLevel(currentLevel + 1);

                // Calcular próxima fecha basada en el nivel
                LocalDateTime nextReview;
                switch (currentLevel) {
                    case 0:
                        nextReview = now.plusDays(1);
                        break;
                    case 1:
                        nextReview = now.plusDays(3);
                        break;
                    case 2:
                        nextReview = now.plusDays(7);
                        break;
                    case 3:
                        nextReview = now.plusDays(14);
                        break;
                    case 4:
                        nextReview = now.plusDays(30);
                        break;
                    default:
                        nextReview = now.plusDays(60);
                        break;
                }
                flashcard.setNextReviewDate(nextReview);
                break;
        }
    }

    @Transactional
    public List<FlashcardDto> saveGeneratedFlashcards(Long collectionId, List<FlashcardDto> flashcards, String userId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        // Obtenemos el usuario del workspace que creo la Flashcard
        User user = collection.getWorkspace().getWorkspaceUsers().stream()
                .map(WorkspaceUser::getUser)
                .filter(u -> u.getId().equals(userId))  
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Flashcard> savedFlashcards = flashcards.stream()
                .map(dto -> {
                    Flashcard flashcard = new Flashcard();
                    flashcard.setQuestion(dto.getQuestion());
                    flashcard.setAnswer(dto.getAnswer());
                    flashcard.setCollection(collection);
                    flashcard.setRepetitionLevel(0);
                    flashcard.setNextReviewDate(LocalDateTime.now());
                    flashcard.setCreatedBy(user);
                    return flashcardRepository.save(flashcard);
                })
                .collect(Collectors.toList());

        return savedFlashcards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private void updateFlashcardFromDto(Flashcard flashcard, FlashcardDto flashcardDto) {
        if (flashcardDto.getQuestion() != null) {
            flashcard.setQuestion(flashcardDto.getQuestion());
        }
        if (flashcardDto.getAnswer() != null) {
            flashcard.setAnswer(flashcardDto.getAnswer());
        }
        if (flashcardDto.getStatus() != null) {
            flashcard.setStatus(flashcardDto.getStatus());
        }
        if (flashcardDto.getNotes() != null) {
            flashcard.setNotes(flashcardDto.getNotes());
        }
        if (flashcardDto.getTags() != null) {
            flashcard.setTags(flashcardDto.getTags());
        }
    }

    private double calculateReviewRate(List<Flashcard> flashcards) {
        long reviewedFlashcards = flashcards.stream()
                .filter(f -> f.getLastReviewedAt() != null)
                .count();
        return (double) reviewedFlashcards / flashcards.size() * 100;
    }

    private double calculateSuccessRate(List<Flashcard> flashcards) {
        long successfulFlashcards = flashcards.stream()
                .filter(f -> f.getKnowledgeLevel() == KnowledgeLevel.BIEN)
                .count();
        return (double) successfulFlashcards / flashcards.size() * 100;
    }

    public FlashcardDto convertToDto(Flashcard flashcard) {
        Hibernate.initialize(flashcard.getCreatedBy());  // Forzar carga de createdBy

        return new FlashcardDto(
                flashcard.getId(),
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                flashcard.getCollection().getId(),
                flashcard.getKnowledgeLevel(),
                flashcard.getRepetitionLevel(),
                flashcard.getNextReviewDate(),
                flashcard.getLastReviewedAt(),
                flashcard.getStatus(),
                flashcard.getNotes(),
                flashcard.getTags(),
                flashcard.getCreatedBy().toDto(),  // 🔹 Convertimos a DTO
                flashcard.getCreatedAt(),
                flashcard.getUpdatedAt()
        );
    }
}
