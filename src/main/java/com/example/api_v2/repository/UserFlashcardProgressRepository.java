package com.example.api_v2.repository;

import com.example.api_v2.model.UserFlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, Long> {
    Optional<UserFlashcardProgress> findByFlashcardIdAndUserId(Long flashcardId, String userId);
    
    List<UserFlashcardProgress> findTop10ByUserIdOrderByLastReviewedAtDesc(String userId);
    
    List<UserFlashcardProgress> findByUserIdAndLastReviewedAtBetween(
        String userId, 
        LocalDateTime start, 
        LocalDateTime end
    );
}
