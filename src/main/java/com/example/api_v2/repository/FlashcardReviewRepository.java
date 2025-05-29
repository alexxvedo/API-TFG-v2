package com.example.api_v2.repository;

import com.example.api_v2.model.FlashcardReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardReviewRepository extends JpaRepository<FlashcardReview, Long> {
    List<FlashcardReview> findByFlashcardId(Long flashcardId);
    
    @Query("SELECT COUNT(fr) FROM FlashcardReview fr WHERE fr.flashcard.id = :flashcardId AND fr.userId = :userId")
    Long countByFlashcardIdAndUserId(
        @Param("flashcardId") Long flashcardId, 
        @Param("userId") String userId
    );
    
    @Query("SELECT COUNT(fr) FROM FlashcardReview fr JOIN fr.flashcard f WHERE f.collection.id = :collectionId AND fr.userId = :userId AND fr.reviewedAt >= :date")
    Long countByFlashcardCollectionIdAndUserIdAndReviewedAtAfter(
        @Param("collectionId") Long collectionId, 
        @Param("userId") String userId, 
        @Param("date") LocalDateTime date
    );
    
    @Query("SELECT fr FROM FlashcardReview fr JOIN fr.flashcard f WHERE f.collection.id = :collectionId AND fr.userId = :userId ORDER BY fr.reviewedAt DESC")
    List<FlashcardReview> findByFlashcardCollectionIdAndUserIdOrderByReviewedAtDesc(
        @Param("collectionId") Long collectionId, 
        @Param("userId") String userId
    );
}