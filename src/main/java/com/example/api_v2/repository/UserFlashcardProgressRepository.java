package com.example.api_v2.repository;


import com.example.api_v2.model.UserFlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, Long> {
    @Query("SELECT ufp FROM UserFlashcardProgress ufp WHERE ufp.flashcard.id = :flashcardId AND ufp.user.id = :userId")
    Optional<UserFlashcardProgress> findByFlashcardIdAndUserId(
            @Param("flashcardId") Long flashcardId,
            @Param("userId") String userId);

    @Query("SELECT ufp FROM UserFlashcardProgress ufp WHERE ufp.user.id = :userId ORDER BY ufp.lastReviewedAt DESC")
    List<UserFlashcardProgress> findTop10ByUserIdOrderByLastReviewedAtDesc(@Param("userId") String userId);

    @Query("SELECT ufp FROM UserFlashcardProgress ufp WHERE ufp.user.id = :userId AND ufp.lastReviewedAt BETWEEN :start AND :end")
    List<UserFlashcardProgress> findByUserIdAndLastReviewedAtBetween(
            @Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<UserFlashcardProgress> findByCollectionIdAndUserId(
            @Param("collectionId") Long collectionId,
            @Param("userId") String userId);

    @Query("SELECT SUM(ufp.studyTimeInSeconds) FROM UserFlashcardProgress ufp JOIN ufp.collection c WHERE c.id = :collectionId AND ufp.user.id = :userId")
    Long sumStudyTimeByCollectionIdAndUserId(
            @Param("collectionId") Long collectionId,
            @Param("userId") String userId);

    @Query("SELECT ufp FROM UserFlashcardProgress ufp WHERE ufp.flashcard.id = :flashcardId AND ufp.collection.id = :collectionId AND ufp.user.id = :userId")
    Optional<UserFlashcardProgress> findByFlashcardIdAndCollectionIdAndUserId(
            @Param("flashcardId") Long flashcardId, 
            @Param("collectionId") Long collectionId, 
            @Param("userId") String userId);
            
    @Query("SELECT ufp FROM UserFlashcardProgress ufp WHERE ufp.flashcard.id = :flashcardId AND ufp.collection.id = :collectionId AND ufp.user.id = :userId")
    List<UserFlashcardProgress> findAllByFlashcardIdAndCollectionIdAndUserId(
            @Param("flashcardId") Long flashcardId, 
            @Param("collectionId") Long collectionId, 
            @Param("userId") String userId);
}
