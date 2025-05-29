package com.example.api_v2.service;

import com.example.api_v2.dto.FlashcardStatsDto;
import com.example.api_v2.model.Flashcard;
import com.example.api_v2.model.FlashcardReview;
import com.example.api_v2.model.KnowledgeLevel;
import com.example.api_v2.model.Collection;
import com.example.api_v2.repository.FlashcardRepository;
import com.example.api_v2.repository.FlashcardReviewRepository;
import com.example.api_v2.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardReviewRepository flashcardReviewRepository;
    private final CollectionRepository collectionRepository;

    /*public FlashcardStatsDto getCollectionStats(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        List<Flashcard> flashcards = flashcardRepository.findByCollection(collection);
        
        if (flashcards.isEmpty()) {
            return createEmptyStats();
        }

        FlashcardStatsDto stats = new FlashcardStatsDto();
        
        // Obtener todas las revisiones de las flashcards de esta colección
        List<FlashcardReview> allReviews = new ArrayList<>();
        for (Flashcard flashcard : flashcards) {
            allReviews.addAll(flashcardReviewRepository.findByFlashcardId(flashcard.getId()));
        }

        // Fechas para filtrado
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOf7DaysAgo = now.toLocalDate().minusDays(7).atStartOfDay();
        LocalDateTime startOf30DaysAgo = now.toLocalDate().minusDays(30).atStartOfDay();

        // Estadísticas de creación
        stats.setCreadasHoy(countFlashcardsCreatedAfter(flashcards, startOfToday));
        stats.setCreadasUltimos7Dias(countFlashcardsCreatedAfter(flashcards, startOf7DaysAgo));
        stats.setCreadasUltimos30Dias(countFlashcardsCreatedAfter(flashcards, startOf30DaysAgo));
        stats.setTotalCreadas(flashcards.size());

        // Estadísticas de revisión
        stats.setRevisadasHoy(countReviewsAfter(allReviews, startOfToday));
        stats.setRevisadasUltimos7Dias(countReviewsAfter(allReviews, startOf7DaysAgo));
        stats.setRevisadasUltimos30Dias(countReviewsAfter(allReviews, startOf30DaysAgo));
        stats.setTotalRevisadas(allReviews.size());

        // Estadísticas de estado
        Map<String, Long> statusCounts = flashcards.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getStatus() != null ? f.getStatus() : "SIN_HACER",
                        Collectors.counting()
                ));
        
        List<FlashcardStatsDto.FlashcardStatusCount> estadosList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            FlashcardStatsDto.FlashcardStatusCount statusCount = new FlashcardStatsDto.FlashcardStatusCount();
            statusCount.setStatus(entry.getKey());
            statusCount.setCount(entry.getValue().intValue());
            statusCount.setPorcentaje((double) entry.getValue() * 100 / flashcards.size());
            estadosList.add(statusCount);
        }
        stats.setEstadosPorStatus(estadosList);
        stats.setStatusCounts(statusCounts);

        // Estadísticas de conocimiento
        Map<KnowledgeLevel, Long> knowledgeLevelCounts = flashcards.stream()
                .filter(f -> f.getKnowledgeLevel() != null)
                .collect(Collectors.groupingBy(
                        Flashcard::getKnowledgeLevel,
                        Collectors.counting()
                ));
        
        long flashcardsWithKnowledgeLevel = knowledgeLevelCounts.values().stream().mapToLong(Long::longValue).sum();
        long flashcardsWithoutKnowledgeLevel = flashcards.size() - flashcardsWithKnowledgeLevel;
        
        if (flashcardsWithoutKnowledgeLevel > 0) {
            knowledgeLevelCounts.put(null, flashcardsWithoutKnowledgeLevel);
        }
        
        List<FlashcardStatsDto.FlashcardKnowledgeCount> knowledgeList = new ArrayList<>();
        for (Map.Entry<KnowledgeLevel, Long> entry : knowledgeLevelCounts.entrySet()) {
            FlashcardStatsDto.FlashcardKnowledgeCount knowledgeCount = new FlashcardStatsDto.FlashcardKnowledgeCount();
            knowledgeCount.setKnowledgeLevel(entry.getKey());
            knowledgeCount.setCount(entry.getValue().intValue());
            knowledgeCount.setPorcentaje((double) entry.getValue() * 100 / flashcards.size());
            knowledgeList.add(knowledgeCount);
        }
        stats.setEstadosPorConocimiento(knowledgeList);
        stats.setKnowledgeLevelCounts(knowledgeLevelCounts);

        // Estadísticas de rendimiento
        long completadas = flashcards.stream()
                .filter(f -> "COMPLETADA".equals(f.getStatus()))
                .count();
        stats.setPorcentajeCompletadas((double) completadas * 100 / flashcards.size());

        long correctAnswers = allReviews.stream()
                .filter(r -> "CORRECT".equals(r.getResult()))
                .count();
        stats.setPorcentajeExito(allReviews.isEmpty() ? 0 : (double) correctAnswers * 100 / allReviews.size());

        OptionalDouble avgTimeSpent = allReviews.stream()
                .filter(r -> r.getTimeSpentMs() != null)
                .mapToLong(FlashcardReview::getTimeSpentMs)
                .average();
        stats.setTiempoMedioRevision(avgTimeSpent.orElse(0) / 1000); // Convert to seconds

        // Rachas y logros
        Set<LocalDate> studyDays = allReviews.stream()
                .map(r -> r.getReviewedAt().toLocalDate())
                .collect(Collectors.toSet());
        
        int currentStreak = calculateCurrentStreak(studyDays);
        int longestStreak = calculateLongestStreak(studyDays);
        
        stats.setRachaActual(currentStreak);
        stats.setRachaMasLarga(longestStreak);
        stats.setDiasTotalesEstudio(studyDays.size());

        // Estadísticas adicionales
        stats.setTotalCards((long) flashcards.size());
        stats.setTotalFlashcards((long) flashcards.size());
        stats.setReviewedFlashcards((long) flashcards.stream()
                .filter(f -> f.getLastReviewedAt() != null)
                .count());
        stats.setReviewRate(stats.getTotalFlashcards() == 0 ? 0 : 
                (double) stats.getReviewedFlashcards() * 100 / stats.getTotalFlashcards());
        stats.setSuccessRate(stats.getPorcentajeExito());
        stats.setDueForReview((long) flashcards.stream()
                .filter(f -> f.getNextReviewDate() != null && f.getNextReviewDate().isBefore(now))
                .count());

        return stats;
    }*/

    private int countFlashcardsCreatedAfter(List<Flashcard> flashcards, LocalDateTime dateTime) {
        return (int) flashcards.stream()
                .filter(f -> f.getCreatedAt() != null && f.getCreatedAt().isAfter(dateTime))
                .count();
    }

    private int countReviewsAfter(List<FlashcardReview> reviews, LocalDateTime dateTime) {
        return (int) reviews.stream()
                .filter(r -> r.getReviewedAt() != null && r.getReviewedAt().isAfter(dateTime))
                .count();
    }

    private int calculateCurrentStreak(Set<LocalDate> studyDays) {
        if (studyDays.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // Check if studied today or yesterday
        boolean studiedToday = studyDays.contains(today);
        boolean studiedYesterday = studyDays.contains(yesterday);
        
        if (!studiedToday && !studiedYesterday) {
            return 0; // Streak broken
        }
        
        LocalDate lastDay = studiedToday ? today : yesterday;
        int streak = 1; // Count today/yesterday
        
        for (int i = 1; i <= 365; i++) { // Check up to a year back
            LocalDate previousDay = lastDay.minusDays(i);
            if (studyDays.contains(previousDay)) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }

    private int calculateLongestStreak(Set<LocalDate> studyDays) {
        if (studyDays.isEmpty()) {
            return 0;
        }
        
        List<LocalDate> sortedDays = new ArrayList<>(studyDays);
        Collections.sort(sortedDays);
        
        int currentStreak = 1;
        int maxStreak = 1;
        
        for (int i = 1; i < sortedDays.size(); i++) {
            LocalDate currentDay = sortedDays.get(i);
            LocalDate previousDay = sortedDays.get(i - 1);
            
            if (ChronoUnit.DAYS.between(previousDay, currentDay) == 1) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }
        
        return maxStreak;
    }

    private FlashcardStatsDto createEmptyStats() {
        FlashcardStatsDto stats = new FlashcardStatsDto();
        stats.setCreadasHoy(0);
        stats.setCreadasUltimos7Dias(0);
        stats.setCreadasUltimos30Dias(0);
        stats.setTotalCreadas(0);
        stats.setRevisadasHoy(0);
        stats.setRevisadasUltimos7Dias(0);
        stats.setRevisadasUltimos30Dias(0);
        stats.setTotalRevisadas(0);
        stats.setEstadosPorStatus(new ArrayList<>());
        stats.setEstadosPorConocimiento(new ArrayList<>());
        stats.setPorcentajeCompletadas(0);
        stats.setPorcentajeExito(0);
        stats.setTiempoMedioRevision(0);
        stats.setRachaActual(0);
        stats.setRachaMasLarga(0);
        stats.setDiasTotalesEstudio(0);
        stats.setTotalCards(0L);
        stats.setStatusCounts(new HashMap<>());
        stats.setKnowledgeLevelCounts(new HashMap<>());
        stats.setTotalFlashcards(0L);
        stats.setReviewedFlashcards(0L);
        stats.setReviewRate(0.0);
        stats.setSuccessRate(0.0);
        stats.setDueForReview(0L);
        return stats;
    }
}
