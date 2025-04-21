package com.example.api_v2.service;

import com.example.api_v2.dto.*;
import com.example.api_v2.model.KnowledgeLevel;
import com.example.api_v2.model.UserFlashcardProgress;
import com.example.api_v2.repository.UserFlashcardProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserActivityService {
    private final UserFlashcardProgressRepository userFlashcardProgressRepository;

    public List<UserActivityDto> getRecentActivity(String userId) {
        List<UserActivityDto> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Obtener las últimas revisiones de flashcards
        List<UserFlashcardProgress> recentProgress = userFlashcardProgressRepository
            .findTop10ByUserIdOrderByLastReviewedAtDesc(userId);

        for (UserFlashcardProgress progress : recentProgress) {
            UserActivityDto activity = new UserActivityDto();
            activity.setType("study");
            activity.setTitle("Study Session");
            activity.setDescription("Reviewed flashcard: " + progress.getFlashcard().getQuestion());
            activity.setTimestamp(progress.getLastReviewedAt());
            
            // Calcular tiempo relativo
            activity.setTime(formatRelativeTime(progress.getLastReviewedAt(), now));

            // Añadir estadísticas
            Map<String, Object> stats = new HashMap<>();
            stats.put("studyTimeInSeconds", progress.getStudyTimeInSeconds());
            stats.put("result", progress.getKnowledgeLevel() != null ? progress.getKnowledgeLevel().toString() : "UNKNOWN");
            activity.setStats(stats);

            activities.add(activity);
        }

        return activities;
    }

    public UserWeeklyStatsDto getWeeklyStats(String userId) {
        UserWeeklyStatsDto stats = new UserWeeklyStatsDto();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        // Obtener todas las revisiones de la última semana
        List<UserFlashcardProgress> weekProgress = userFlashcardProgressRepository
            .findByUserIdAndLastReviewedAtBetween(userId, weekAgo, now);

        // Calcular estadísticas
        int totalCards = weekProgress.size();
        int totalTimeInSeconds = weekProgress.stream()
            .filter(p -> p.getStudyTimeInSeconds() != null)
            .mapToInt(UserFlashcardProgress::getStudyTimeInSeconds)
            .sum();
        long correctAnswers = weekProgress.stream()
            .filter(p -> p.getKnowledgeLevel() != null && p.getKnowledgeLevel() == KnowledgeLevel.BIEN)
            .count();

        // Formatear tiempo de estudio
        int hours = totalTimeInSeconds / 3600;
        int minutes = (totalTimeInSeconds % 3600) / 60;
        stats.setStudyTime(String.format("%dh %dm", hours, minutes));
        stats.setCardsStudied(totalCards);
        stats.setAccuracy(String.format("%.0f%%", totalCards > 0 ? (correctAnswers * 100.0 / totalCards) : 0));

        // Calcular heatmap
        Map<LocalDate, List<UserFlashcardProgress>> progressByDay = weekProgress.stream()
            .collect(Collectors.groupingBy(p -> p.getLastReviewedAt().toLocalDate()));
        
        List<UserWeeklyStatsDto.DayActivity> heatmap = new ArrayList<>();
        LocalDateTime nowLocal = LocalDateTime.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = nowLocal.minusDays(i).toLocalDate();
            List<UserFlashcardProgress> dayProgress = progressByDay.getOrDefault(date, Collections.emptyList());
            
            int minutesStudied = dayProgress.stream()
                    .filter(p -> p.getStudyTimeInSeconds() != null)
                    .mapToInt(UserFlashcardProgress::getStudyTimeInSeconds)
                    .sum() / 60;
            
            int cardsStudied = dayProgress.size();
            
            double accuracy = dayProgress.stream()
                    .filter(p -> p.getKnowledgeLevel() != null && p.getKnowledgeLevel() == KnowledgeLevel.BIEN)
                    .count() * 100.0 / (cardsStudied > 0 ? cardsStudied : 1);

            UserWeeklyStatsDto.DayActivity dayActivity = new UserWeeklyStatsDto.DayActivity();
            dayActivity.setDay(date.getDayOfWeek().toString().substring(0, 3));
            dayActivity.setMinutesStudied(minutesStudied);
            dayActivity.setCardsStudied(cardsStudied);
            dayActivity.setAccuracy(accuracy);
            dayActivity.setIntensity(getIntensityLevel(minutesStudied));
            dayActivity.setAchievements(Collections.emptyList()); // TODO: Add achievements for the day
            
            heatmap.add(dayActivity);
        }
        stats.setHeatmap(heatmap);

        // Calcular días activos
        stats.setDaysActive((int) weekProgress.stream()
            .map(p -> p.getLastReviewedAt().toLocalDate())
            .distinct()
            .count());

        return stats;
    }

    public UserMonthlyStatsDto getMonthlyStats(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        List<UserFlashcardProgress> monthProgress = userFlashcardProgressRepository
            .findByUserIdAndLastReviewedAtBetween(userId, monthStart, now);

        UserMonthlyStatsDto stats = new UserMonthlyStatsDto();
        
        // Calcular tiempo total de estudio
        int totalMinutes = monthProgress.stream()
            .filter(p -> p.getStudyTimeInSeconds() != null)
            .mapToInt(UserFlashcardProgress::getStudyTimeInSeconds)
            .sum() / 60;
        
        stats.setStudyTime(formatStudyTime(totalMinutes));
        
        // Calcular tarjetas estudiadas
        stats.setCardsStudied(monthProgress.size());
        
        // Calcular precisión
        long correctAnswers = monthProgress.stream()
            .filter(p -> p.getKnowledgeLevel() != null && p.getKnowledgeLevel() == KnowledgeLevel.BIEN)
            .count();
        int totalCards = monthProgress.size();
        stats.setAccuracy(String.format("%.0f%%", totalCards > 0 ? (correctAnswers * 100.0 / totalCards) : 0));
        
        // Calcular días activos
        Set<LocalDate> activeDays = monthProgress.stream()
            .map(p -> p.getLastReviewedAt().toLocalDate())
            .collect(Collectors.toSet());
        stats.setDaysActive(activeDays.size());

        // Calcular heatmap
        Map<LocalDate, List<UserFlashcardProgress>> progressByDay = monthProgress.stream()
            .collect(Collectors.groupingBy(p -> p.getLastReviewedAt().toLocalDate()));
        
        List<UserMonthlyStatsDto.DayActivity> heatmap = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(now);
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = monthStart.plusDays(i).toLocalDate();
            List<UserFlashcardProgress> dayProgress = progressByDay.getOrDefault(date, Collections.emptyList());
            
            int minutesStudied = dayProgress.stream()
                    .filter(p -> p.getStudyTimeInSeconds() != null)
                    .mapToInt(UserFlashcardProgress::getStudyTimeInSeconds)
                    .sum() / 60;
            
            int cardsStudied = dayProgress.size();
            
            double accuracy = dayProgress.stream()
                    .filter(p -> p.getKnowledgeLevel() != null && p.getKnowledgeLevel() == KnowledgeLevel.BIEN)
                    .count() * 100.0 / (cardsStudied > 0 ? cardsStudied : 1);

            UserMonthlyStatsDto.DayActivity dayActivity = new UserMonthlyStatsDto.DayActivity();
            dayActivity.setDay(date.getDayOfWeek().toString().substring(0, 3));
            dayActivity.setDayOfMonth(date.getDayOfMonth());
            dayActivity.setWeekOfMonth((date.getDayOfMonth() - 1) / 7 + 1);
            dayActivity.setMinutesStudied(minutesStudied);
            dayActivity.setCardsStudied(cardsStudied);
            dayActivity.setAccuracy(accuracy);
            dayActivity.setIntensity(getIntensityLevel(minutesStudied));
            dayActivity.setAchievements(Collections.emptyList());
            
            heatmap.add(dayActivity);
        }
        stats.setHeatmap(heatmap);

        return stats;
    }

    private String formatRelativeTime(LocalDateTime time, LocalDateTime now) {
        long minutes = ChronoUnit.MINUTES.between(time, now);
        if (minutes < 60) return minutes + "m ago";
        
        long hours = ChronoUnit.HOURS.between(time, now);
        if (hours < 24) return hours + "h ago";
        
        long days = ChronoUnit.DAYS.between(time, now);
        return days + "d ago";
    }

    private String formatStudyTime(int minutes) {
        int hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%dh %dm", hours, minutes);
    }

    private int getIntensityLevel(int minutesStudied) {
        if (minutesStudied == 0) return 0;
        if (minutesStudied < 30) return 1;
        if (minutesStudied < 60) return 2;
        return 3;
    }
}
